package co.winda.models

import java.time.LocalDateTime

import com.vividsolutions.jts.geom.Point

/**
* @author David Karigithu
* @since 13/06/16
*/
case class Stadium(id: Option[Long], name: String, description: String, location: Point, created: Option[LocalDateTime],
                   modified: Option[LocalDateTime], teamId: Option[Long], countryId: Long)