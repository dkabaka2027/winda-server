package co.winda.models

import java.time.LocalDateTime

import co.winda.enums.CountryType

/**
* @author David Karigithu
* @since 13/06/16
*/
case class Country(id: Option[Long], name: String, description: String, code: String, `type`: CountryType.Value,
                   created: Option[LocalDateTime], modified: Option[LocalDateTime], parent: Option[Long])