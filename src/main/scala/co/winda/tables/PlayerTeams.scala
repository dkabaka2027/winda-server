package co.winda.tables

import co.winda.models.PlayerTeam
import java.time.{LocalDate, LocalDateTime}

import co.winda.Tables.{players, teams}
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._

class PlayerTeams(tag: Tag) extends Table[PlayerTeam](tag, "player_teams") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def buyingPrice = column[Long]("buyingPrice")
  def weeklySalary = column[Long]("weeklySalary")
  def from = column[LocalDate]("from")
  def to = column[Option[LocalDate]]("to")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def currentStat = column[Long]("currentStat")
  def playerId = column[Long]("playerId")
  def teamId = column[Long]("teamId")

  def * = (id.?, buyingPrice, weeklySalary, from, to, created, modified, currentStat, playerId, teamId) <> (PlayerTeam.tupled, PlayerTeam.unapply)

  def player = foreignKey("TEAM_PLAYER", playerId, players)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def team = foreignKey("PLAYER_TEAM", teamId, teams)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}
