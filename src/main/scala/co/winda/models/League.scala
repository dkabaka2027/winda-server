package co.winda.models

import java.time.LocalDateTime

/**
* @author David Karigithu
* @since 13/06/16
*/
case class League(id: Option[Long], key: String, name: String, description: String, icon: String, club: Boolean, created: Option[LocalDateTime],
                  modified: Option[LocalDateTime], countryId: Long)