package co.winda.common.modules

import com.typesafe.config.{Config, ConfigFactory}

/**
*
*
*/
trait ConfigModule {
  val config: Config
}

class Configuration extends ConfigModule {
  val config: Config = ConfigFactory.load("application.conf")
}