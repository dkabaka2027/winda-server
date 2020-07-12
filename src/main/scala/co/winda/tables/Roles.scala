package co.winda.tables

import akka.pattern._
import java.time.LocalDateTime

import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.common.slick.base.BaseTable
import co.winda.models.{Permission, Role}

/**
* @author David Karigithu
* @since 21-09-2016
*/
class Roles(tag: Tag) extends Table[Role](tag, "roles") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def description = column[String]("description")
  def tenant = column[Boolean]("tenant")
  def permissions = column[List[String]]("permissions")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))

  def * = (id.?, name, description, tenant, permissions, created, modified) <> (Role.tupled, Role.unapply)
}