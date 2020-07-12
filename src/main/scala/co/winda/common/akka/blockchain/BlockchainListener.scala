package co.winda.common.akka.blockchain

import java.net.InetSocketAddress

import akka.pattern._
import akka.io.Tcp.Bind
import akka.io.{IO, Tcp}
import akka.actor.Actor.Receive
import com.typesafe.config.Config
import slick.driver.PostgresDriver
import akka.actor.{Actor, ActorContext, ActorLogging, ActorSystem}
import akka.util.Timeout

/**
* TODO: Extend BlockChain Listener into Actor to Handle Events
* @author David Karigithu
* @since 22/09/16
*/
trait BlockchainListener extends Actor with ActorLogging {

  implicit val context: ActorContext
  implicit val system: ActorSystem
  implicit val config: Config
  implicit val db: PostgresDriver#Backend#Database
  implicit val profile: PostgresDriver
  implicit val timeout: Timeout

  /**
  * Receive
  * @return
  */
  override def receive: Receive = {
    case "" =>
    case "" =>
    case "" =>
  }

  IO(Tcp) ? Bind(
    self,
    new InetSocketAddress(
      config.getString("akka.ethereum.rpcaddr"), config.getInt("akka.ethereum.rpcport")
    )
  )

}
