package co.winda.models

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseEntity

case class GameStatistic(id: Option[Long], possession: Float, shots: Float, blocked: Float, corners: Float, fouls: Float,
                         chances: Float, chancesMissed: Float, counters: Float, saves: Float, passes: Float, dribbles: Float,
                         dispossessed: Float, duels: Float, aerials: Float, tackles: Float, Floaterceptions: Float, clearances: Float,
                         created: Option[LocalDateTime], modified: Option[LocalDateTime], teamId: Long, gameId: Long) extends BaseEntity[Long]
