package co.winda.models

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseEntity

case class PlayerStatistic(id: Option[Long], pace: Int, shooting: Int, passing: Int, dribbling: Int, defense: Int, physical: Int,
                           created: Option[LocalDateTime], modified: Option[LocalDateTime], playerId: Long) extends BaseEntity[Long]
