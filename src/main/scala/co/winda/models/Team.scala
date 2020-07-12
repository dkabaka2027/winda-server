package co.winda.models

import java.time.{LocalDate, LocalDateTime}

import co.winda.common.slick.base.BaseEntity

/**
* @author David Karigithu
* @since 13/06/16
*/
case class Team(id: Option[Long], key: String, title: String, altTitle: Option[String], history: String, code: String, icon: String,
                synonyms: Option[String], club: Boolean, since: Option[LocalDate], address: Option[String], web: Option[String],
                created: Option[LocalDateTime], modified: Option[LocalDateTime], leagueId: Long, cityId: Option[Long],
                countryId: Long) extends BaseEntity[Long]