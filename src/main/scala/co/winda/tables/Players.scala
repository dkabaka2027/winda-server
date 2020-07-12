package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.teams
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.enums.PositionType
import co.winda.models.{Player, PlayerStatistic}

class Players(tag: Tag) extends Table[Player](tag, "players") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def bio = column[String]("bio")
  def image = column[String]("image")
  def position = column[PositionType.Value]("position")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def internationalTeamId = column[Long]("internationalTeamId")
  def teamId = column[Long]("teamId")

  def * = (id.?, name, bio, image, position, created, modified, internationalTeamId, teamId) <> (Player.tupled, Player.unapply)

  def international = foreignKey("PLAYER_TEAM_INT", internationalTeamId, teams)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def team = foreignKey("PLAYER_TEAM", teamId, teams)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}