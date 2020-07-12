package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.players
import co.winda.models.PlayerStatistic
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._

class PlayerStatistics(tag: Tag) extends Table[PlayerStatistic](tag, "player_statistics") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def pace = column[Int]("pace")
  def shooting = column[Int]("shooting")
  def passing = column[Int]("passing")
  def dribbling = column[Int]("dribbling")
  def defense = column[Int]("defense")
  def physical = column[Int]("physical")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def playerId = column[Long]("playerId")

  def * =  (id.?, pace, shooting, passing, dribbling, defense, physical, created, modified, playerId) <> (PlayerStatistic.tupled, PlayerStatistic.unapply)

  def player = foreignKey("STATISTICS_PLAYER", playerId, players)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)

}
