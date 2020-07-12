package co.winda.tables

import co.winda.models.Team
import java.time.{LocalDate, LocalDateTime}
import co.winda.common.slick.base.BaseTable
import co.winda.Tables.{countries, leagues, stadiums}
import co.winda.common.slick.driver.PostgresGeoDriver.api._

class Teams(tag: Tag) extends Table[Team](tag, "teams") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def key = column[String]("key")
  def title = column[String]("title")
  def altTitle = column[Option[String]]("altTitle")
  def history = column[String]("history")
  def code = column[String]("code")
  def icon = column[String]("icon")
  def synonyms = column[Option[String]]("synonyms")
  def club = column[Boolean]("club")
  def since = column[Option[LocalDate]]("since")
  def address = column[Option[String]]("address")
  def web = column[Option[String]]("web")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def leagueId = column[Long]("leagueId")
  def cityId = column[Option[Long]]("cityId")
  def countryId = column[Long]("countryId")

  def * = (id.?, key, title, altTitle, history, code, icon, synonyms, club, since, address, web, created, modified,
    leagueId, cityId, countryId) <> (Team.tupled, Team.unapply)

  def league = foreignKey("TEAM_LEAGUE", leagueId, leagues)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def city = foreignKey("TEAM_CITY", cityId, countries)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def country = foreignKey("TEAM_COUNTRY", countryId, countries)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)

}
