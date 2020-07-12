package co.winda.schedulers

import java.time.{LocalDateTime, ZoneOffset}

import akka.util.Timeout
import co.winda.Tables.tokens

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}
import co.winda.common.slick.driver.PostgresGeoDriver
import slick.dbio.NoStream
import slick.jdbc.JdbcBackend

case object AuthenticationInit

class Authentication(
  implicit system: ActorSystem,
  implicit val db: PostgresGeoDriver#Backend#Database,
  implicit val profile: PostgresGeoDriver,
  implicit val ec: ExecutionContext,
  implicit val timeout: Timeout
) extends Actor with ActorLogging{
  import profile.api._

  def apply()(implicit system: ActorSystem, db: PostgresGeoDriver#Backend#Database, profile: PostgresGeoDriver, ec: ExecutionContext, timeout: Timeout) = {
    this
  }

  override def receive: Receive = {
    case AuthenticationInit =>
      db.run(tokens.filter(t => t.active === true && t.created <= LocalDateTime.now() && t.created >= LocalDateTime.parse("15m")).result.transactionally).map { ts =>
        db.run(DBIO.seq[Effect.Write](ts.foldRight(Seq[DBIOAction[_, NoStream, Effect.Write]]()) { (a, b) =>
          if (a.ttl > (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - a.created.get.toEpochSecond(ZoneOffset.UTC)))
            b ++: Seq[DBIOAction[_, NoStream, Effect.Write]](tokens.filter(_.id === a.id).update(a.copy(active = false)))
          b
        }:_*))
      }
  }

}