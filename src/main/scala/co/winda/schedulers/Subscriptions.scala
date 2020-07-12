package co.winda.schedulers

import java.time.LocalDateTime
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}
import akka.util.Timeout
import co.winda.Tables.{orders, subscriptions, transactions, users}
import co.winda.common.slick.driver.PostgresGeoDriver
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.models.{Order, Subscription, Transaction, User}
import slick.jdbc.JdbcBackend

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

case object SubscriptionInit

class Subscriptions(
  implicit system: ActorSystem,
  implicit val db: PostgresGeoDriver#Backend#Database,
  implicit val profile: PostgresGeoDriver,
  implicit val ec: ExecutionContext,
  implicit val timeout: Timeout
) extends Actor with ActorLogging {
  import profile.api._

  def apply()(implicit system: ActorSystem, db: PostgresGeoDriver#Backend#Database, profile: PostgresGeoDriver, ec: ExecutionContext, timeout: Timeout) = {
    this
  }

  override def receive: Receive = {
    case SubscriptionInit =>
      // TODO: Search for orders and join with subscription
      val action = for {
        // TODO: Fetch last time the scheduler was run
        os <- orders.filter(o => o.created <= LocalDateTime.now() && o.created >= LocalDateTime.now())
          .join(subscriptions).on((o, s) => o.subscriptionId === s.id).result.transactionally
        us <- users.filter(_.id.inSet(os.foldRight(Seq[Long]())((a, b) => b ++: Seq(a._1.userId)))).result.transactionally
        ts <- transactions.filter(_.orderId.inSet(os.foldRight(Seq[Long]())((a, b) => b ++: Seq(a._1.userId)))).result.transactionally
      } yield os.map(o => (o._1, ts.find(_.orderId == o._1.id.get), o._2, us.find(_.id == o._1.userId).get))
      db.run(action).map { os: Seq[(Order, Option[Transaction], Subscription, User)] =>
        // TODO: Check users paymentStatus and invalidate if necessary
        os.map {
          case (order, Some(transaction), subscription, user) =>
            // TODO: If transaction status is complete and duration of sub is 5 days longer than the last transaction modified time
            if (transaction.status == "COMPLETE" || LocalDateTime.now().minus(subscription.duration, ChronoUnit.DAYS).compareTo(transaction.modified.get) > 0)
              db.run(users returning users.map(_.id) += user.copy(status = "UNPAID")).map {
                case d if d > 0 =>
                case _ =>
              }
          case (order, None, subscription, user) =>
            if (user.status == "PAID")
              db.run(users returning users.map(_.id) += user.copy(status = "UNPAID")).map {
                case d if d > 0 =>
                case _ =>
              }
        }
      }
  }

}