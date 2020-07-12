package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.{games, teams}
import co.winda.models.Squad
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._

class Squads(tag: Tag) extends Table[Squad](tag, "squads") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def teamId = column[Long]("teamId")
  def gameId = column[Long]("gameId")

  def * = (id.?, created, modified, teamId, gameId) <> (Squad.tupled, Squad.unapply)

  def team = foreignKey("SQUAD_TEAM", teamId, teams)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def game = foreignKey("SQUAD_GAME", gameId, games)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}
