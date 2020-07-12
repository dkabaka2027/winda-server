package co.winda.common.modules

import co.winda.common.slick.driver.PostgresGeoDriver
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile

/**
*
*
*/
trait DBModule {
  val db: PostgresGeoDriver#Backend#Database
  val profile: PostgresGeoDriver
}

class Persistence extends Configuration with DBModule {
  private val dbConfig: DatabaseConfig[PostgresGeoDriver] = DatabaseConfig.forConfig[PostgresGeoDriver]("database", config, getClass.getClassLoader)
//    .asInstanceOf[DatabaseConfig[PostgresGeoDriver]]

  lazy val db: PostgresGeoDriver#Backend#Database = dbConfig.db
  lazy val profile: PostgresGeoDriver = dbConfig.profile

  // private val session = db.createSession()
  // try session.force() finally session.close()
}