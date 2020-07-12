package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.{countries, leagues}
import co.winda.common.slick.base.BaseTable
import co.winda.models.Season
import co.winda.common.slick.driver.PostgresGeoDriver.api._

class Seasons(tag: Tag) extends Table[Season](tag, "seasons") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def start = column[LocalDateTime]("start")
  def end = column[LocalDateTime]("end")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def countryId = column[Long]("countryId")
  def leagueId = column[Long]("leagueId")

  def * = (id.?, start, end, created, modified, countryId, leagueId) <> (Season.tupled, Season.unapply)

  def country = foreignKey("SEASON_COUNTRY", countryId, countries)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def league = foreignKey("SEASON_LEAGUE", leagueId, leagues)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}
