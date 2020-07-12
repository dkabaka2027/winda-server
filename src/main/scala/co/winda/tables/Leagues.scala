package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.countries
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.models.League

class Leagues(tag: Tag) extends Table[League](tag,"leagues") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def key = column[String]("key")
  def name = column[String]("name")
  def description = column[String]("description")
  def icon = column[String]("icon")
  def club = column[Boolean]("club")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def countryId = column[Long]("countryId")

  def * = (id.?, key, name, description, icon, club, created, modified, countryId) <> (League.tupled, League.unapply)

  def country = foreignKey("LEAGUE_COUNTRY", countryId, countries)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}
