package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.{players, squads}
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.models.PlayerSquad

class PlayerSquads(tag: Tag) extends Table[PlayerSquad](tag, "player_squads") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def starting = column[Boolean]("starting")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def currentStat = column[Long]("currentStat")
  def playerId = column[Long]("playerId")
  def squadId = column[Long]("squadId")

  def * = (id.?, starting, created, modified, currentStat, playerId, squadId) <> (PlayerSquad.tupled, PlayerSquad.unapply)

  def player = foreignKey("SQUAD_PLAYER", playerId, players)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def squad = foreignKey("PLAYER_SQUAD", squadId, squads)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}
