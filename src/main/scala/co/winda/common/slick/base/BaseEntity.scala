package co.winda.common.slick.base

import java.net.InetSocketAddress
import java.time.LocalDateTime

import akka.actor.Props
import akka.io.IO
import akka.io.Tcp.Bind
import akka.pattern._
import co.winda.common.modules.{ActorModule, Persistence}

/**
* @author David Karigithu
* @since 21-09-2016
*/
trait BaseEntity[PK] {
  val id: Option[PK]
  val created: Option[LocalDateTime]
  val modified: Option[LocalDateTime]
}