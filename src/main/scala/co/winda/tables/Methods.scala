package co.winda.tables

import java.time.LocalDateTime

import co.winda.models.Method
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._

class Methods(tag: Tag) extends Table[Method](tag, "methods") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def description = column[String]("description")
  def code = column[String]("code")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))

  def * = (id.?, name, description, code, created, modified) <> (Method.tupled, Method.unapply)

}
