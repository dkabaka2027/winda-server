package co.winda.android

import java.time.LocalDateTime
import co.winda.enums.GameStatus
import co.winda.models.{Game, Player, Team, GameStatistic}

case class GameResponse(homeTeam: Team, awayTeam: Team, play: LocalDateTime, homeStats: GameStatistic, awayStats: GameStatistic,
                        status: GameStatus.Value, homePrediction: Option[Double], drawPrediction: Option[Double],
                        awayPrediction: Option[Double], homeScore: Option[Int], awayScore: Option[Int])
