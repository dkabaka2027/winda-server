package co.winda.common.akka.worker

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSystem}
import com.typesafe.config.Config
import slick.driver.PostgresDriver

/**
* @author David Karigithu
* @since 26-09-2016
*/
abstract class BaseWorker(
  actorRef: ActorRef
)(
  implicit context: ActorContext,
  implicit val system: ActorSystem,
  implicit val config: Config,
  implicit val db: PostgresDriver#Backend#Database,
  implicit val profile: PostgresDriver
) extends Actor with ActorLogging {

  /**
  * Receive
  * @return
  */
  def receive: Receive = ???

}
