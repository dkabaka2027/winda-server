package co.winda.models

import java.time.LocalDateTime

case class Token(id: Option[Long], token: String, ttl: Int, active: Boolean, created: Option[LocalDateTime], ownedBy: Long)
