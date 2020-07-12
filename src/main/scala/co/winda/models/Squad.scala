package co.winda.models

import java.time.LocalDateTime

/**
* @author David Karigithu
* @since 13/06/16
*/
case class Squad(id: Option[Long], created: Option[LocalDateTime], modified: Option[LocalDateTime], teamId: Long, gameId: Long)
