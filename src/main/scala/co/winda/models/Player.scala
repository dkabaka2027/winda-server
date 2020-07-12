package co.winda.models

import java.time.LocalDateTime

import co.winda.enums.PositionType

/**
* @author David Karigithu
* @since 13/06/16
*/
case class Player(id: Option[Long], name: String, bio: String, image: String, position: PositionType.Value, created: Option[LocalDateTime],
                  modified: Option[LocalDateTime], internationalTeamId: Long, teamId: Long)