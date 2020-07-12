package co.winda.common.modules

import akka.actor.ActorSystem
import java.lang.ExceptionInInitializerError

/**
*
*
*/
trait ActorModule extends Configuration {

  implicit val system: ActorSystem = ActorSystem(config.getString("akka.system"), config)

}