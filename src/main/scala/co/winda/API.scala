package co.winda

import akka.io.IO
import spray.json._
import akka.pattern._
import co.winda.repos._
import akka.util.{ByteString, Timeout}
import co.winda.models._
import co.winda.android._

import scala.util.Success
import slick.dbio.DBIOAction
import co.winda.enums.GameStatus
import akka.http.scaladsl.model._
import com.typesafe.config.Config
import akka.stream.ActorMaterializer
import co.winda.common.security.models._
import co.winda.common.security.Security
import co.winda.Tables.{players, teams, _}
import java.time.{LocalDateTime, ZoneOffset}

import akka.actor.{ActorContext, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives.{entity, path, _}
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import co.winda.common.security.enums.Algorithm
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.model.headers.HttpCredentials
import co.winda.common.slick.extensions.QueryExtensions._
import akka.http.scaladsl.server.directives.OnSuccessMagnet
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshal, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.scaladsl.{Flow, Keep}
import co.winda.common.slick.driver.PostgresGeoDriver
import com.coiney.akka.mailer.MailerSystem
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.{DefaultConsumerService, RequestWithInfo}

/**
* @author David Karigithu
* @since 21-09-2016
*/
class API(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val mailer: MailerSystem,
  implicit val config: Config,
  implicit val db: PostgresGeoDriver#Backend#Database,
  implicit val profile: PostgresGeoDriver,
  implicit val ec: ExecutionContext,
  implicit val timeout: Timeout
) extends JsonProtocol with SecurityDirectives with Security {

  import profile.api._

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json()

  implicit def um[T](implicit reader: RootJsonFormat[T]): FromRequestUnmarshaller[T] =
    Unmarshaller.withMaterializer[HttpRequest, T](implicit ec ⇒ implicit mat ⇒ { req ⇒
      if (jsonStreamingSupport.supported.matches(req.entity.contentType)) {
        val frames = req.entity.dataBytes.via(jsonStreamingSupport.framingDecoder)
        val unmarshal = (bytes: ByteString) => Unmarshal(bytes).to[T](sprayJsonByteStringUnmarshaller[T], ec, mat)
        val unmarshallingFlow =
          if (jsonStreamingSupport.unordered) Flow[ByteString].mapAsyncUnordered(jsonStreamingSupport.parallelism)(unmarshal)
          else Flow[ByteString].mapAsync(jsonStreamingSupport.parallelism)(unmarshal)
        val elements = frames.viaMat(unmarshallingFlow)(Keep.right)
        elements.runReduce[T] { (a, b) =>
          a
        }(mat)
      } else FastFuture.failed(Unmarshaller.UnsupportedContentTypeException(jsonStreamingSupport.supported))
    })

  implicit object LocalDateTimeOrdering extends Ordering[LocalDateTime] {
    def compare(a: LocalDateTime, b: LocalDateTime): Int = a.compareTo(b)
  }

  implicit def rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handleNotFound {
        extractUnmatchedPath { p =>
          complete((NotFound, Message(404, s"The path you requested [${p}] does not exist.")))
        }
      }
      .result()

  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case ex: Exception =>
        complete(Message(500, ex.getMessage()))
    }

  def router: Route = {
    path("") {
      getFromResource("ui/index.html")
    } ~
    path("assets" / Segment) { path =>
      getFromResource(s"ui/assets/$path")
    } ~
    pathPrefix("api") {
      path("games") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(GameRepo.fetchAll()))) {
              case data: Seq[Game] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.findOptionById(id)))) {
                case Some(data: Game) =>
                  complete(ToResponseMarshallable(data))
                case None =>
                  complete("{}")
              }
            }
        } ~
        post {
          pathEndOrSingleSlash {
            entity(as[Game]) { entity: Game =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.save(entity)))) { data: Game =>
                complete((201, data))
              }
            }
          }
        } ~
        put {
          path(LongNumber) { id: Long =>
            entity(as[Game]) { entity: Game =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.update(entity)))) { data: Game =>
                complete((201, data))
              }
            }
          }
        } ~
        delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      } ~
      path("leagues") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(LeagueRepo.fetchAll()))) {
              case data: Seq[League] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
          path(LongNumber) { id: Long =>
            onSuccess(OnSuccessMagnet(db.run(LeagueRepo.findOptionById(id)))) {
              case Some(data: League) =>
                complete(ToResponseMarshallable(data))
              case None =>
                complete("{}")
            }
          }
        } ~
        post {
          pathEndOrSingleSlash {
            entity(as[League]) { entity: League =>
              onSuccess(OnSuccessMagnet(db.run(LeagueRepo.save(entity)))) { data: League =>
                complete((201, data))
              }
            }
          }
        } ~
        put {
          path(LongNumber) { id: Long =>
            entity(as[League]) { entity: League =>
              onSuccess(OnSuccessMagnet(db.run(LeagueRepo.update(entity)))) { data: League =>
                complete((201, data))
              }
            }
          }
        } ~
        delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(LeagueRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
    } ~
      path("methods") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(GameRepo.fetchAll()))) {
              case data: Seq[Game] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.findOptionById(id)))) {
                case Some(data: Game) =>
                  complete(ToResponseMarshallable(data))
                case None =>
                  complete("{}")
              }
            }
        } ~
          post {
            pathEndOrSingleSlash {
              entity(as[Game]) { entity: Game =>
                onSuccess(OnSuccessMagnet(db.run(GameRepo.save(entity)))) { data: Game =>
                  complete((201, data))
                }
              }
            }
          } ~
          put {
            path(LongNumber) { id: Long =>
              entity(as[Game]) { entity: Game =>
                onSuccess(OnSuccessMagnet(db.run(GameRepo.update(entity)))) { data: Game =>
                  complete((201, data))
                }
              }
            }
          } ~
          delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      } ~
      path("news") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(GameRepo.fetchAll()))) {
              case data: Seq[Game] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.findOptionById(id)))) {
                case Some(data: Game) =>
                  complete(ToResponseMarshallable(data))
                case None =>
                  complete("{}")
              }
            }
        } ~
          post {
            pathEndOrSingleSlash {
              entity(as[Game]) { entity: Game =>
                onSuccess(OnSuccessMagnet(db.run(GameRepo.save(entity)))) { data: Game =>
                  complete((201, data))
                }
              }
            }
          } ~
          put {
            path(LongNumber) { id: Long =>
              entity(as[Game]) { entity: Game =>
                onSuccess(OnSuccessMagnet(db.run(GameRepo.update(entity)))) { data: Game =>
                  complete((201, data))
                }
              }
            }
          } ~
          delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      } ~
      path("players") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(PlayerRepo.fetchAll()))) {
              case data: Seq[Player] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(PlayerRepo.findOptionById(id)))) {
                case Some(data: Player) =>
                  complete(ToResponseMarshallable(data))
                case None =>
                  complete("{}")
              }
            }
        } ~
          post {
            pathEndOrSingleSlash {
              entity(as[Player]) { entity: Player =>
                onSuccess(OnSuccessMagnet(db.run(PlayerRepo.save(entity)))) { data: Player =>
                  complete((201, data))
                }
              }
            }
          } ~
          put {
            path(LongNumber) { id: Long =>
              entity(as[Player]) { entity: Player =>
                onSuccess(OnSuccessMagnet(db.run(PlayerRepo.update(entity)))) { data: Player =>
                  complete((201, data))
                }
              }
            }
          } ~
          delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(PlayerRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      } ~
      path("seasons") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(GameRepo.fetchAll()))) {
              case data: Seq[Game] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.findOptionById(id)))) {
                case Some(data: Game) =>
                  complete(ToResponseMarshallable(data))
                case None =>
                  complete("{}")
              }
            }
        } ~
          post {
            pathEndOrSingleSlash {
              entity(as[Game]) { entity: Game =>
                onSuccess(OnSuccessMagnet(db.run(GameRepo.save(entity)))) { data: Game =>
                  complete((201, data))
                }
              }
            }
          } ~
          put {
            path(LongNumber) { id: Long =>
              entity(as[Game]) { entity: Game =>
                onSuccess(OnSuccessMagnet(db.run(GameRepo.update(entity)))) { data: Game =>
                  complete((201, data))
                }
              }
            }
          } ~
          delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      } ~
      path("stadiums") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(StadiumRepo.fetchAll()))) {
              case data: Seq[Stadium] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(StadiumRepo.findOptionById(id)))) {
                case Some(data: Stadium) =>
                  complete(ToResponseMarshallable(data))
                case None =>
                  complete("{}")
              }
            }
        } ~
          post {
            pathEndOrSingleSlash {
              entity(as[Stadium]) { entity: Stadium =>
                onSuccess(OnSuccessMagnet(db.run(StadiumRepo.save(entity)))) { data: Stadium =>
                  complete((201, data))
                }
              }
            }
          } ~
          put {
            path(LongNumber) { id: Long =>
              entity(as[Stadium]) { entity: Stadium =>
                onSuccess(OnSuccessMagnet(db.run(StadiumRepo.update(entity)))) { data: Stadium =>
                  complete((201, data))
                }
              }
            }
          } ~
          delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(StadiumRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      } ~
      path("subscriptions") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(GameRepo.fetchAll()))) {
              case data: Seq[Game] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.findOptionById(id)))) {
                case Some(data: Game) =>
                  complete(ToResponseMarshallable(data))
                case None =>
                  complete("{}")
              }
            }
        } ~
          post {
            pathEndOrSingleSlash {
              entity(as[Game]) { entity: Game =>
                onSuccess(OnSuccessMagnet(db.run(GameRepo.save(entity)))) { data: Game =>
                  complete((201, data))
                }
              }
            }
          } ~
          put {
            path(LongNumber) { id: Long =>
              entity(as[Game]) { entity: Game =>
                onSuccess(OnSuccessMagnet(db.run(GameRepo.update(entity)))) { data: Game =>
                  complete((201, data))
                }
              }
            }
          } ~
          delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(GameRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      }
      path("teams") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(TeamRepo.fetchAll()))) {
              case data: Seq[Team] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(TeamRepo.findOptionById(id)))) {
                case Some(data: Team) =>
                  complete(ToResponseMarshallable(data))
                case None =>
                  complete("{}")
              }
            }
        } ~
          post {
            pathEndOrSingleSlash {
              entity(as[Team]) { entity: Team =>
                onSuccess(OnSuccessMagnet(db.run(TeamRepo.save(entity)))) { data: Team =>
                  complete((201, data))
                }
              }
            }
          } ~
          put {
            path(LongNumber) { id: Long =>
              entity(as[Team]) { entity: Team =>
                onSuccess(OnSuccessMagnet(db.run(TeamRepo.update(entity)))) { data: Team =>
                  complete((201, data))
                }
              }
            }
          } ~
          delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(TeamRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      }
      path("transactions") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(TransactionRepo.fetchAll()))) {
              case data: Seq[Transaction] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
          path(LongNumber) { id: Long =>
            onSuccess(OnSuccessMagnet(db.run(TransactionRepo.findOptionById(id)))) {
              case Some(data: Transaction) =>
                complete(ToResponseMarshallable(data))
              case None =>
                complete("{}")
            }
          }
        } ~
        post {
          pathEndOrSingleSlash {
            entity(as[Transaction]) { entity: Transaction =>
              onSuccess(OnSuccessMagnet(db.run(TransactionRepo.save(entity)))) { data: Transaction =>
                complete((201, data))
              }
            }
          }
        } ~
        put {
          path(LongNumber) { id: Long =>
            entity(as[Transaction]) { entity: Transaction =>
              onSuccess(OnSuccessMagnet(db.run(TransactionRepo.update(entity)))) { data: Transaction =>
                complete((201, data))
              }
            }
          }
        } ~
        delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(TransactionRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      }
      path("users") {
        get {
          pathEndOrSingleSlash {
            onSuccess(OnSuccessMagnet(db.run(UserRepo.fetchAll()))) {
              case data: Seq[User] =>
                complete(data)
              case _ =>
                complete("{}")
            }
          } ~
          path(LongNumber) { id: Long =>
            onSuccess(OnSuccessMagnet(db.run(UserRepo.findOptionById(id)))) {
              case Some(data: User) =>
                complete(ToResponseMarshallable(data))
              case None =>
                complete("{}")
            }
          }
        } ~
        post {
          pathEndOrSingleSlash {
            entity(as[User]) { entity: User =>
              onSuccess(OnSuccessMagnet(db.run(UserRepo.save(entity)))) { data: User =>
                complete((201, data))
              }
            }
          }
        } ~
        put {
          path(LongNumber) { id: Long =>
            entity(as[User]) { entity: User =>
              onSuccess(OnSuccessMagnet(db.run(UserRepo.update(entity)))) { data: User =>
                complete((201, data))
              }
            }
          }
        } ~
        delete {
            path(LongNumber) { id: Long =>
              onSuccess(OnSuccessMagnet(db.run(UserRepo.deleteById(id)))) { count =>
                complete((201, count))
              }
            }
          }
      }
    } ~
    pathPrefix("security") {
      extractCredentials { creds =>
        path("login") {
          post {
            entity(as[Credential]) { login =>
              onSuccess(OnSuccessMagnet(db.run(
                users.filter(u => u.username === login.credential || u.email === login.credential).result.headOption.transactionally
              ))) {
                case Some(user: User) =>
                  val password = hash(generateSecureRandom(), login.password, Algorithm.SHA256)
                  if (user.password == password.toString() && user.status == "ACTIVE") {
                    // Create Login Token and Return JWT
                    val sr = generateSecureRandom().nextInt(64)
                    val t = Token(None, sr.toString, login.ttl, true, None, user.id.get)
                    val j = JWT(t.token, login.ttl, LocalDateTime.now())
                    val jwtT = JwtToken(j, user)
                    complete(ToResponseMarshallable(jwtT))
                  } else {
                    complete(ToResponseMarshallable(Message(201, "Unable to login, please check your password!")))
                  }
                case _ =>
                  complete(ToResponseMarshallable(Message(404, "Something went wrong")))
              }
            }
          }
        } ~
          path("/logout") {
            get {
              onSuccess(OnSuccessMagnet {
                creds match {
                  case Some(c) =>
                    db.run(tokens.filter(_.token === c.token()).result.headOption.transactionally)
                  case None =>
                    throw new RuntimeException
                }
              }) {
                case Some(t) =>
                  if (t.ttl > (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - t.created.get.toEpochSecond(ZoneOffset.UTC))) {
                    db.run(tokens.filter(_.token === t.token).update(t.copy(active = false)).transactionally)
                    complete(ToResponseMarshallable(Message(201, "Successfully logged out!")))
                  }
                  complete(ToResponseMarshallable(Message(404, "No active session to logout from!")))
                case _ =>
                  complete(ToResponseMarshallable(Message(304, "")))
              }
            }
          } ~
          path("reset") {
            post {
              entity(as[Reset]) { reset =>
                onSuccess(OnSuccessMagnet {
                  db.run(users.filter(u => u.username === reset.credential || u.email === reset.credential).result.headOption.transactionally).map {
                    case Some(u: User) =>
                      val t = Token(None, "", 3600000, true, None, u.id.get)
                      db.run((tokens returning tokens.map(_.id) += t).transactionally).map { id =>
                        // TODO: Send email to user with url to password reset form
                        Some(t)
                      }
                    case None =>
                      None
                  }
                }) {
                  case Some(t: Token) =>
                    complete(Message(201, "Successfully requested for a password reset. Please check your email for further instructions"))
                  case _ =>
                    complete(Message(304, "Failed to request for a password reset!"))
                }
              }
            }
          } ~
          path("confirm" / Segment) { token =>
            post {
              entity(as[Confirm]) { confirm =>
                onSuccess(OnSuccessMagnet {
                  db.run(tokens.filter(_.token === token).result.headOption.transactionally).map {
                    case Some(t: Token) =>
                      // TODO: Check if token is still valid
                      if (t.active && t.ttl > (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - t.created.get.toEpochSecond(ZoneOffset.UTC))) {
                        // TODO: Fetch user
                        db.run(users.filter(_.id === t.ownedBy).result.headOption.transactionally).map { u =>
                          // TODO: Invalidate Token
                          db.run(tokens.filter(_.id === t.id).update(t.copy(active = false)))
                          u
                        }
                      } else {
                        None
                      }
                    case None =>
                      None
                  }
                }) {
                  case Some(u: User) =>
                    // TODO: Change password
                    complete(Message(201, ""))
                  case _ =>
                    complete(Message(304, "Failed to reset your password!"))
                }
              }
            }
          } ~
          path("registration") {
            post {
              entity(as[Registration]) { registration =>
                onSuccess(OnSuccessMagnet {
                  db.run(users.filter(u => u.username === registration.username && u.email === registration.email).result.headOption.transactionally)
                }) {
                  case Some(user: User) => complete(ToResponseMarshallable(Message(203, "Cannot register, a user with same email or username already exists!")))
                  case None => {
                    val password = hash(generateSecureRandom(), registration.password, Algorithm.SHA256)
                    val user = User(None, registration.firstName, registration.lastName, registration.fullName, registration.username,
                      registration.email, registration.telephone, password.toString(), "", Algorithm.SHA256, "ACTIVE", None, None, 1)
                    db.run((users returning users.map(_.id) += user).transactionally)
                    complete(ToResponseMarshallable(Message(201, s"Successfully registered user with username ${registration.username}!")))
                  }
                  case _ =>
                    complete("")
                }
              }
            }
          }
      }
    } ~
    pathPrefix("android") {
      path("home") {
        get {
          parameters('page.as[Int], 'count.as[Int]) { (page: Int, count: Int) =>
            onSuccess(OnSuccessMagnet {
              val action = for {
                gs <- games.page(page, count).result.transactionally
                ns <- news.page(page, count).result.transactionally
                rs <- games.page(page, count).result.transactionally
                ts <- teams.filter(_.id.inSet(rs.foldRight(Seq[Long]())((a, b) => b ++: Seq(a.homeTeamId, a.awayTeamId)))).page(page, count).result.transactionally
              } yield (ns, gs, rs.map(r => (r, ts.filter(t => t.id.get == r.homeTeamId && t.id.get == r.awayTeamId))))

              db.run(action)
            }) {
              case data: (Seq[News], Seq[Game], Seq[(Game, Seq[Team])]) =>
                complete(HomeResponse(data._1, data._2, data._3.map(g => ResultResponse(
                  g._2.head,
                  g._2.tail.head,
                  g._1.homeScore.get,
                  g._1.awayScore.get,
                  g._1.homePrediction.get,
                  g._1.drawPrediction.get,
                  g._1.awayPrediction.get
                ))))
              case _ =>
                complete("")
            }
          }
        }
      }
      path("games") {
        get {
          parameters('page.as[Int], 'count.as[Int]) { (page: Int, count: Int) =>
            onSuccess(OnSuccessMagnet {
              val action: DBIO[Seq[(Game, Seq[GameStatistic], Seq[Team], Seq[Player])]] = for {
                gs <- games.page(page, count).result.transactionally
                stats <- gameStatistics.filter(st => st.id.inSet(gs.foldRight(Seq[Long]())((a, b) => b ++: Seq(a.homeTeamId, a.awayTeamId)))).result.transactionally
                ts <- teams.filter(_.id.inSet(gs.foldRight(Seq[Long]())((a, b) => b ++: Seq(a.homeTeamId, a.awayTeamId)))).result.transactionally
                ps <- players.filter(_.id.inSet(gs.foldRight(Seq[Long]())((a, b) => b ++: Seq(a.homeTeamId, a.awayTeamId)))).result.transactionally
              } yield gs.map(g => (
                g,
                stats.filter(t => t.teamId == g.homeTeamId && t.teamId == g.awayTeamId && t.gameId == g.id.get),
                ts.filter(t => t.id.get == g.homeTeamId && t.id.get == g.awayTeamId),
                ps.filter(p => p.teamId == g.homeTeamId && p.teamId == g.awayTeamId)
              ))
              db.run(action)
            }) {
              case data: Seq[(Game, Seq[GameStatistic], Seq[Team],  Seq[Player])] =>
                complete(data.map { item: (Game, Seq[GameStatistic], Seq[Team], Seq[Player]) =>
                  GameResponse(
                    item._3.head,
                    item._3.tail.head,
                    item._1.play,
                    item._2.head,
                    item._2.tail.head,
                    item._1.status,
                    item._1.homePrediction,
                    item._1.drawPrediction,
                    item._1.awayPrediction,
                    item._1.homeScore,
                    item._1.awayScore
                  )
                })
              case _ =>
                complete("")
            }
          }
        }
      } ~
      path("games"/ LongNumber) { gameId: Long =>
        onSuccess(OnSuccessMagnet {
          val action: DBIO[(Game, Seq[Team], Seq[Player])] = for {
            gs <- games.filter(_.id === gameId).result.head.transactionally
            ts <- teams.filter(_.id.inSet(Seq(gs.homeTeamId, gs.awayTeamId))).result.transactionally
            ps <- players.filter(_.id.inSet(Seq(gs.homeTeamId, gs.awayTeamId))).result.transactionally
          } yield (
            gs,
            ts.filter(t => t.id.get == gs.homeTeamId && t.id.get == gs.awayTeamId),
            ps.filter(p => p.teamId == gs.homeTeamId && p.teamId == gs.awayTeamId)
          )
          db.run(action)
        }) {
          case data: (Game, Seq[Team], Seq[Player]) =>
            complete(GameItemResponse(data._1, data._3, Seq.empty[Head2Head], data._2))
        }
      } ~
      path("results") {
        get {
          parameters('page.as[Int], 'count.as[Int]) { (page: Int, count: Int) =>
            onSuccess(OnSuccessMagnet {
              // TODO: Fix issue with filter - _.status === GameStatus.PLAYED
              val action = for {
                gs <- games.page(page, count).result.transactionally
                ts <- teams.filter(_.id.inSet(gs.foldRight(Seq[Long]())((a, b) => b ++: Seq(a.homeTeamId, a.awayTeamId)))).result.transactionally
              } yield gs.map(g => (g, ts.filter(t => t.id.get == g.homeTeamId && t.id.get == g.awayTeamId)))
              db.run(action)
            }) {
              case data: Seq[(Game, Seq[Team])] =>
                complete(data.map { d: (Game, Seq[Team]) =>
                  ResultResponse(
                    d._2.find(_.id.get == d._1.homeTeamId).get,
                    d._2.find(_.id.get == d._1.awayTeamId).get,
                    d._1.homeScore.get,
                    d._1.awayScore.get,
                    d._1.homePrediction.get,
                    d._1.drawPrediction.get,
                    d._1.awayPrediction.get
                  )
                })
              case _ => complete("")
            }
          }
        }
      } ~
      path("head2head") {
        get {
          onSuccess(OnSuccessMagnet(db.run(
            games.join(teams).on((g, t) => g.awayTeamId === t.id && g.homeTeamId === t.id)
              .result.transactionally
          ))) {
            case data =>
              complete((200, ""))
            case _ =>
              complete("")
          }
        }
      } ~
      path("payments") {
        get {
          parameters('page.as[Int], 'count.as[Int]) { (page: Int, count: Int) =>
            extractCredentials { case Some(cred: HttpCredentials) =>
              onSuccess(OnSuccessMagnet {
                val action = for {
                  t <- tokens.filter(_.token === cred.token()).result.head.transactionally
                  trans <- transactions.page(page, count).filter(_.userId === t.ownedBy).result.transactionally
                } yield  trans
                db.run(action)
//                db.run(tokens.filter(_.token === cred.token()).result.headOption.transactionally).map { case Some(t: Token) =>
//                  db.run(transactions.page(page, count).filter(_.userId === t.ownedBy).result.transactionally)
//                }
              }) {
                case data =>
                  complete((200, data))
                case _ =>
                  complete("")
              }
            }
          }
        }
      } ~
      // TODO: Players and Team
      path("players") {
        get {
          onSuccess(OnSuccessMagnet(db.run(players.join(teams).on((p, t) => p.teamId === t.id).result.transactionally))) {
            case players =>
              complete(players.map(res => PlayerResponse(res._1, res._2)))
            case _ =>
              complete("")
          }
        }
      } ~
      path("leagues") {
        parameters('page.as[Int], 'count.as[Int]) { (page: Int, count: Int) =>
          onSuccess(OnSuccessMagnet {
            db.run(leagues.page(page,count).result.transactionally)
          }) {
            case data =>
              complete(data)
            case _ =>
              complete("")
          }
        }
      } ~
      path("leagues" / Segment) { leagueName =>
        get {
          onSuccess(OnSuccessMagnet {
            val action: DBIO[(League, Season, Seq[Game], Seq[Team])] = for {
              ls <- leagues.filter(_.name === leagueName).result.head.transactionally
              ss <- seasons.filter(s => s.leagueId === ls.id).result.transactionally
              gs <- games.filter(g => g.seasonId.inSet(ss.foldRight(Seq[Long]())((a, b) => b ++: Seq(a.id.get)))).result.transactionally
              ts <- teams.filter(t => t.id.inSet(gs.foldRight(Seq[Long]())((a, b) => b ++: Seq(a.homeTeamId, a.awayTeamId)))).result.transactionally
            } yield {
              val s = ss.filter(_.leagueId == ls.id.get).sortBy(_.start).head
              (
                ls,
                s,
                gs.filter(_.seasonId == s.id.get),
                ts.filter(_.leagueId == ls.id.get)
              )
            }
            db.run(action)
          }) {
            case data: (League, Season, Seq[Game], Seq[Team]) =>
              complete(LeagueResponse(
                data._1,
                data._2,
                data._4.map { tm =>
                  LeagueItem(
                    tm,
                    data._3.count(g => g.homeTeamId == tm.id.get || g.awayTeamId == tm.id.get),
                    data._3.foldRight(0) { (a, b) =>
                      if (a.homeTeamId == tm.id.get && a.homeScore.get > a.awayScore.get)
                        b + 1
                      if (a.awayTeamId == tm.id.get && a.awayScore.get > a.homeScore.get)
                        b + 1
                      b
                    },
                    data._3.count(g => (g.homeTeamId == tm.id.get || g.awayTeamId == tm.id.get) && g.homeScore == g.awayScore),
                    data._3.foldRight(0) { (a, b) =>
                      if (a.homeTeamId == tm.id.get && a.homeScore.get < a.awayScore.get)
                        b + 1
                      if (a.awayTeamId == tm.id.get && a.awayScore.get < a.homeScore.get)
                        b + 1
                      b
                    },
                    data._3.filter(g => g.homeTeamId == tm.id.get || g.awayTeamId == tm.id.get).foldRight(0) { (a, b) =>
                      if (a.homeTeamId == tm.id.get && a.homeScore.get > a.awayScore.get)
                        b + 3
                      if (a.awayTeamId == tm.id.get && a.awayScore.get > a.homeScore.get)
                        b + 3
                      if (a.awayScore.get == a.homeScore.get)
                        b + 1
                      b
                    }
                  )
                }
              ))
            case _ =>
              complete("")
          }
        }
      } ~
      path("payments" / "methods") {
        get {
          parameters('page.as[Int], 'count.as[Int]) { (page: Int, count: Int) =>
            extractCredentials { creds =>
              onSuccess(OnSuccessMagnet(db.run(methods.page(page, count).result.transactionally))) {
                case data =>
                  complete(data)
                case _ =>
                  complete(Message(304, ""))
              }
            }
          }
        }
      } ~
      path("payment") {
        post {
          entity(as[Payment]) { payment =>
            onSuccess(OnSuccessMagnet {
              val consumerKey = config.getString("payments.key")
              val consumerSecret = config.getString("payments.secret")
              val apiKey = config.getString("payments.apiKey")
              val request: RequestWithInfo = DefaultConsumerService.createOauthenticatedRequest(
                KoauthRequest(
                  HttpMethods.POST.value,
                  "https://www.pesapal.com/API/PostPesapalDirectOrderV4",
                  Some(
                    s"""<PesapalDirectOrderInfo
                       |xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       |xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                       |Amount="${payment.amount}"
                       |Currency="${payment.currency}"
                       |Description="Order payment for order ACD09"
                       |Type="MERCHANT"
                       |Reference="${payment.reference}"
                       |FirstName="${payment.firstName}"
                       |LastName="${payment.lastName}"
                       |Email="${payment.email}"
                       |PhoneNumber="${payment.telephone}"
                       |xmlns="http://www.pesapal.com">
                       |<LineItems>
                       | <LineItem UniqueId="${payment.subscription.id}" Particulars="${payment.subscription.name}" Quantity="1" UnitCost="${payment.subscription.cost}" SubTotal="${payment.subscription.cost}" />
                       |</LineItems>
                       |</PesapalDirectOrderInfo>""".stripMargin)
                ),
                consumerKey,
                consumerSecret,
                "",
                ""
              )

              // TODO:
              Http().singleRequest(HttpRequest(HttpMethod.custom(request.request.method), Uri(request.request.urlWithoutParams)))
            }) {
              case response: HttpResponse =>
                // Additional Processing
                // TODO: Parse response for iframe url
                // TODO: Twirl Template with an iFrame
                complete(response.entity.dataBytes)
            }
          }
        }
      } ~
      path("payments" / "ipn") {
        post {
          entity(as[String]) { payload: String =>
            val parsed: Array[Array[String]] = payload.split("&").map(_.split("="))
            val ref: String = parsed(2)(1)
            onSuccess(OnSuccessMagnet {
              db.run(transactions.filter(_.reference === ref).result.headOption.transactionally).map {
                case Some(t: Transaction) =>
                  db.run(transactions.filter(_.reference === ref).update(t.copy(status = "COMPLETE")).transactionally)
                case None =>
                  throw new RuntimeException
              }
            }) {
              case response =>
                complete("")
              case _ =>
                complete("")
            }
          }
        }
      }
    }
  }
}