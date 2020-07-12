package co.winda.models

import java.time.LocalDateTime

/**
* @author David Karigithu
* @since 13/06/16
*/
case class PlayerSquad(id: Option[Long], starting: Boolean, created: Option[LocalDateTime], modified: Option[LocalDateTime],
                       currentStat: Long, playerId: Long, squadId: Long)