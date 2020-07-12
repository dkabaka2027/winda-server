package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.{seasons, teams}
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.enums.{GameStatus, GameType}
import co.winda.models.{Game, GameStatistic}

class Games(tag: Tag) extends Table[Game](tag, "games") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def key = column[String]("key")
  def homeTeamId = column[Long]("homeTeamId")
  def awayTeamId = column[Long]("awayTeamId")
  def homeScore = column[Option[Int]]("homeScore")
  def awayScore = column[Option[Int]]("awayScore")
  def homePrediction = column[Option[Double]]("homePrediction")
  def drawPrediction = column[Option[Double]]("drawPrediction")
  def awayPrediction = column[Option[Double]]("awayPrediction")
  def play = column[LocalDateTime]("play")
  def `type` = column[GameType.Value]("type")
  def status = column[GameStatus.Value]("status")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def seasonId = column[Long]("seasonId")

  def * = (id.?, key, homeTeamId, awayTeamId, homeScore, awayScore, homePrediction, drawPrediction,
    awayPrediction, play, `type`, status, created, modified, seasonId) <> (Game.tupled, Game.unapply)

  def homeTeam = foreignKey("GAME_HOME_TEAM", homeTeamId, teams)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def awayTeam = foreignKey("GAME_AWAY_TEAM", awayTeamId, teams)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def season = foreignKey("GAME_SEASON", awayTeamId, seasons)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)

}
