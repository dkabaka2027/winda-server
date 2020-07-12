package co.winda.models

import java.time.LocalDateTime

/**
* @author David Karigithu
* @since 13/06/16
*/
case class Season(id: Option[Long], start: LocalDateTime, end: LocalDateTime, created: Option[LocalDateTime], modified: Option[LocalDateTime],
                  countryId: Long, leagueId: Long)