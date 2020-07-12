package co.winda.models

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseEntity
import co.winda.enums.GameEventType

/**
* @author David Karigithu
* @since 13/06/16
*/
case class GameEvent(id: Option[Long], `type`: GameEventType.Value, timestamp: LocalDateTime, quadrant: List[Int],
                     created: Option[LocalDateTime], modified: Option[LocalDateTime], gameId: Long) extends BaseEntity[Long]