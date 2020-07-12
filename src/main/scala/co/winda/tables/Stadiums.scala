package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.{countries, teams}
import co.winda.common.slick.base.BaseTable
import co.winda.models.Stadium
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import com.vividsolutions.jts.geom.Point

class Stadiums(tag: Tag) extends Table[Stadium](tag, "stadiums") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def description = column[String]("description")
  def location = column[Point]("location")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def teamId = column[Long]("teamId")
  def countryId = column[Long]("countryId")

  def * = (id.?, name, description, location, created, modified, teamId.?, countryId) <> (Stadium.tupled, Stadium.unapply)

  def team = foreignKey("STADIUM_TEAM", teamId, teams)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def country = foreignKey("STADIUM_COUNTRY", countryId, countries)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}