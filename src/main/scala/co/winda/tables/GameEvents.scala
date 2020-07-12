package co.winda.tables

import java.sql.Timestamp
import java.time.LocalDateTime

import co.winda.Tables.{games, leagues, seasons}
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.enums.GameEventType
import co.winda.models.GameEvent

class GameEvents(tag: Tag) extends Table[GameEvent](tag, "game_events") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def `type` = column[GameEventType.Value]("type")
  def timestamp = column[LocalDateTime]("timestamp")
  def quadrant = column[List[Int]]("quadrant")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def gameId = column[Long]("gameId")

  def * = (id.?, `type`, timestamp, quadrant, created, modified, gameId) <> (GameEvent.tupled, GameEvent.unapply)

  def game = foreignKey("LEAGUE_GAME", gameId, games)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)

}