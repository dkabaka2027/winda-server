package co.winda.schedulers

import akka.actor.{Actor, ActorSystem, Props}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

case object ShoppingInit

object Shoppings {

  def apply()(implicit system: ActorSystem, ec: ExecutionContext): Unit = {
    system.scheduler.schedule(0.seconds, 24.hours, system.actorOf(Props(classOf[Shoppings])), ShoppingInit)
  }

}

class Shoppings extends Actor {

  override def receive: Receive = {
    case ShoppingInit =>
  }

}