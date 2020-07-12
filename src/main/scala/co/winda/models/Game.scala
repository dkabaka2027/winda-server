package co.winda.models

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseEntity
import co.winda.enums.{GameStatus, GameType}

/**
* @author David Karigithu
* @since 13/06/16
*/
case class Game(id: Option[Long], key: String, homeTeamId: Long, awayTeamId: Long, homeScore: Option[Int],
                awayScore: Option[Int], homePrediction: Option[Double], drawPrediction: Option[Double],
                awayPrediction: Option[Double], play: LocalDateTime, `type`: GameType.Value, status: GameStatus.Value,
                created: Option[LocalDateTime], modified: Option[LocalDateTime],
                seasonId: Long) extends BaseEntity[Long]