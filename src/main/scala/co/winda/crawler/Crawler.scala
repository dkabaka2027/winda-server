package co.winda.crawler

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.Timeout
import co.winda
import co.winda.Tables._
import co.winda.common.slick.driver.PostgresGeoDriver
import net.kenro.ji.jin.{By, ChromeSession, WebElement}
import spray.json.{JsBoolean, JsNumber, JsObject, JsString, JsValue}
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.enums.{GameEventType, GameStatus, GameType, PositionType}
import com.redis.RedisClient
import com.redis.serialization.Format
import com.vividsolutions.jts.geom.{Coordinate, Point, PrecisionModel}
import models._
import co.winda.models.{Job, _}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.matching.Regex

object Crawler {
  def run(job: Job)(implicit system: ActorSystem, db: PostgresGeoDriver#Backend#Database, profile: PostgresGeoDriver, ec: ExecutionContext, timeout: Timeout): Cancellable = {
    log.info(s"Scheduling Crawler with Job: ${job}")
    system.scheduler.schedule(0.seconds, 24.hours, system.actorOf(Props(new Crawler(job))), Init)
  }

}

class Crawler(job: Job)(
  implicit system: ActorSystem,
  implicit val db: PostgresGeoDriver#Backend#Database,
  implicit val profile: PostgresGeoDriver,
  implicit val ec: ExecutionContext,
  implicit val timeout: Timeout
  // implicit val redis: RedisClient
) extends Actor with ActorLogging {

  val regex: Regex = "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9\\/\\+\\]\\[][a-zA-Z0-9-\\/\\+\\]\\[]+[a-zA-Z0-9\\/\\+\\]\\[]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9\\/\\+\\]\\[]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9\\/\\+\\]\\[]+\\.[^\\s]{2,})".r

  override def receive: Receive = {
    case Init =>
      job.urls.foreach { url =>
        log.info(s"Validating URL: ${url}")
        self ! Validate(url)
      }
    case Validate(url) =>
      log.info(s"Validating URL against Regex: ${url}")
      if (regex.pattern.matcher(url).matches) {
        log.info(s"Sending URL to Download: ${url}")
        self ! Download(url)
      }
    case Download(url) =>
      // TODO: Update url cache
      log.info(s"Downloading URL: ${url}")
      ChromeSession(
        pathToChromeDriver = "C:\\Users\\SNP\\Documents\\chromedriver.exe",
        debug = true
      ) (session => {
        log.info(s"Opening URL: ${url}")
        log.info(s"ChromeDriver Session is: ${session}")
        session.visitUrl(url)

        val element: WebElement = session.waitForClass(job.waitFor)

        // TODO: Match url to a job extractor
        val extractor: Option[Extractor] = job.extractors.find { extractor =>
          val regex = extractor.urlPattern.r
          regex.pattern.matcher(url).matches()
        }
        log.info(s"Extractor: ${extractor}")

        extractor match {
          case Some(ext) =>
            ext.fields.foldRight(Map[String, Any]()){ (a, b) =>
            // TODO: Extract selector from Option
              val f = session.findElement(By.xpath(a.selector.get))
              a.dataType match {
                case "string" =>
                  b.+((a.key, f.getText))
                case "number" =>
                  b.+((a.key, f.getText.toFloat))
                case "boolean" =>
                  b.+((a.key, f.getText.toBoolean))
                case "image" => // TODO: if image then base64 encode binary
                  b.+((a.key, f.getAttribute("src")))
                // case _ => // TODO: If field datatype is none of the above then send to process
              }
            }
          case None => log.debug(s"No extractor found for this URL: ${url}!")
        }
        implicit val booleanFormat = Format[Boolean](java.lang.Boolean.parseBoolean, _.toString)

        // TODO: Collect all links on page
        session.findAll(By.tagName("a")).map { anchor =>
          anchor.getAttribute("href")
        }.andThen { url =>
          self ! Validate(url)
          // redis.get[Boolean](url).map {
          //   case Some(value) =>
          //     if (!value)
          //       redis.set(url, false)
          //   case None =>
          //     redis.set(url, false)
          // }
        }

      })
    case Process(model, data) =>
      model match {
        // TODO: Create Prediction if game play date is older than current date and if game status is not played
        // TODO: Search for object in db, if it exists, don't add it
        case "games" =>
          db.run(for {
            leagueId <- leagues.filter(l => l.name like s"").map(_.id).result.transactionally
            ts <- teams.filter(t => t.title.like(s"%${data("homeTeam").toString}%") || t.title.like(s"%${data("awayTeam").toString}%")).map(_.id).result.transactionally
            seasonId <- seasons.filter(s => s.start >= LocalDateTime.now() && s.end <= LocalDateTime.now).map(_.id).result.head.transactionally
          } yield {
            games returning games += Game(
              None,
              data("key").toString,
              ts.head,
              ts.tail.head,
              Some(data("homeScore").toString.toInt),
              Some(data("awayScore").toString.toInt),
              None,
              None,
              None,
              LocalDateTime.parse(data("play").toString),
              GameType(data("type").toString.toInt),
              GameStatus(data("status").toString.toInt),
              None,
              None,
              seasonId
            )
          }).map { game =>
            data("statistics") match {
              case a: Seq[Map[String, Any]] =>
                self ! Process("gameStatistics", a:_*)
            }

            data("events") match {
              case a: Seq[Map[String, Any]] =>
                self ! Process("gameEvents", a:_*)
            }

            data("squads") match {
              case a: Seq[Map[String, Any]] =>
                self ! Process("playerSquads", a:_*)
            }
          }
        case "leagues" =>
          db.run(for {
            countryId <- countries.filter(_.name like s"%${data("country").toString}%").map(_.id).result.head.transactionally
          } yield {
            leagues returning leagues.map(_.id) += League(
              None,
              data("key").toString,
              data("name").toString,
              data("description").toString,
              data("icon").toString,
              data("club").toString.toBoolean,
              None,
              None,
              countryId
            )
          })
        case "news" =>
          news returning news.map(_.id) += News(
            None,
            data("title").toString,
            data("content").toString,
            data("content").toString.take(60),
            data("image").toString,
            None,
            None
          )
        case "players" =>
          // TODO: Remove team id from player in database
          // TODO: Search for player in database
          // TODO: If player exists update only player statistics
          db.run(for {
            p <- players.filter(_.name like s"%${data("name").toString}%").result.head.transactionally
            t <- teams.filter(_.title like s"%${data("name").toString}%").map(_.id).result.head.transactionally
          } yield {
            players returning players += Player(
              None,
              data("name").toString,
              "",
              data("image").toString,
              PositionType(data("position").toString.toInt),
              None,
              None,
              data("international").toString.toLong,
              t
            )
          }).map { player =>
            data("statistics") match {
              case a: Map[String, Any] =>
                self ! Process("playerStatistics", a)
            }
          }
        case "playerStatistics" =>
          db.run(for {
            playerId <- players.filter(_.name like s"%${data("name").toString}%").map(_.id).result.head.transactionally
          } yield {
            playerStatistics returning playerStatistics.map(_.id) += PlayerStatistic(
              None,
              data("pace").toString.toInt,
              data("shooting").toString.toInt,
              data("passing").toString.toInt,
              data("dribbling").toString.toInt,
              data("defense").toString.toInt,
              data("physical").toString.toInt,
              None,
              None,
              playerId
            )
          })
        case "squads" =>
        case "stadiums" =>
          val location = data("location").toString.stripSuffix("]").stripPrefix("[").split(", ")
          db.run(for {
            teamId <- teams.filter(_.title like s"%${data("team").toString}%").map(_.id).result.head.transactionally
            countryId <- countries.filter(_.name like s"%${data("team").toString}%").map(_.id).result.head.transactionally
          } yield {
            stadiums returning stadiums.map(_.id) += Stadium(
              None,
              data("name").toString,
              data("description").toString,
              new Point(new Coordinate(location(0).toDouble, location(1).toDouble, location(2).toDouble), new PrecisionModel(PrecisionModel.FLOATING), 0),
              None,
              None,
              Some(teamId),
              countryId
            )
          })
        case "team" =>
          db.run(for {
            leagueId <- leagues.filter(_.name like s"%${data("league").toString}%").map(_.id).result.head.transactionally
            countryId <- leagues.filter(_.name like s"%${data("country").toString}%").map(_.id).result.head.transactionally
          } yield {
            teams returning teams.map(_.id) += Team(
              None,
              data("name").toString.replace(" ", "-").toLowerCase,
              data("name").toString,
              None,
              data("history").toString,
              data("name").toString.replace(" ", "-").toLowerCase,
              data("icon").toString,
              None,
              true,
              None,
              None,
              None,
              None,
              None,
              leagueId,
              None,
              countryId
            )
          })
      }
    case Process(model, data @ _*) =>
      model match {
        case "gameEvents" =>
          db.run(for {
            gameId <- games.filter(_.id === 1l).map(_.id).result.head.transactionally
          } yield {
            data.foreach { d =>
              gameEvents returning gameEvents.map(_.id) += GameEvent(
                None,
                GameEventType(d("type").toString.toInt),
                LocalDateTime.parse(d("timestamp").toString),
                List.empty[Int],
                None,
                None,
                gameId
              )
            }
          })
        case "gameStatistics" =>
          db.run(for {
            g <- games.filter(_.id === 1l).result.head.transactionally
          } yield {
            val tuple = data.foldRight((Seq[Float](), Seq[Float]())) { (a: Map[String, Any], b) =>
              val home = b._1.+:(a("home").toString.toFloat)
              val away = b._2.+:(a("away").toString.toFloat)
              (home, away)
            }
            val homeStats = GameStatistic(
              None,
              tuple._1.head,
              tuple._1(1),
              tuple._1(2),
              tuple._1(3),
              tuple._1(4),
              tuple._1(5),
              tuple._1(6),
              tuple._1(7),
              tuple._1(8),
              tuple._1(9),
              tuple._1(10),
              tuple._1(11),
              tuple._1(12),
              tuple._1(13),
              tuple._1(14),
              tuple._1(15),
              tuple._1(16),
              None,
              None,
              g.homeTeamId,
              g.id.get
            )
            val awayStats = GameStatistic(
              None,
              tuple._2.head,
              tuple._2(1),
              tuple._2(2),
              tuple._2(3),
              tuple._2(4),
              tuple._2(5),
              tuple._2(6),
              tuple._2(7),
              tuple._2(8),
              tuple._2(9),
              tuple._2(10),
              tuple._2(11),
              tuple._2(12),
              tuple._2(13),
              tuple._2(14),
              tuple._2(15),
              tuple._2(16),
              None,
              None,
              g.awayTeamId,
              g.id.get
            )

            gameStatistics returning gameStatistics.map(_.id) ++= Seq(homeStats, awayStats)
          })
      }
  }

}