package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.countries
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.enums.CountryType
import co.winda.models.Country

class Countries(tag: Tag) extends Table[Country](tag, "countries") with BaseTable[Long] {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def description = column[String]("description")
  def code = column[String]("code")
  def `type` = column[CountryType.Value]("type")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def parent = column[Option[Long]]("parent")

  def * = (id.?, name, description, code, `type`, created, modified, parent) <> (Country.tupled, Country.unapply)

  def parentKey = foreignKey("COUNTRY_PARENT", parent, countries)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}