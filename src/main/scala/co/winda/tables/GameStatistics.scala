package co.winda.tables

import java.time.LocalDateTime
import co.winda.models.GameStatistic
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._

class GameStatistics(tag: Tag) extends Table[GameStatistic](tag, "game_statistics") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def possession = column[Float]("possession")
  def shots = column[Float]("shots")
  def blocked = column[Float]("blocked")
  def corners = column[Float]("corners")
  def fouls = column[Float]("fouls")
  def chances = column[Float]("chances")
  def chancesMissed = column[Float]("chancesMissed")
  def counters = column[Float]("counters")
  def saves = column[Float]("saves")
  def passes = column[Float]("passes")
  def dribbles = column[Float]("dribbles")
  def dispossessed = column[Float]("dispossessed")
  def duels = column[Float]("duelsWon")
  def aerials = column[Float]("aerialsWon")
  def tackles = column[Float]("tackles")
  def interceptions = column[Float]("interceptions")
  def clearances = column[Float]("clearances")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def teamId = column[Long]("teamId")
  def gameId = column[Long]("gameId")

  def * = (id.?, possession, shots, blocked, corners, fouls, chances, chancesMissed, counters, saves, passes, dribbles,
    dispossessed, duels, aerials, tackles, interceptions, clearances, created, modified, teamId, gameId) <> (GameStatistic.tupled, GameStatistic.unapply)
}
