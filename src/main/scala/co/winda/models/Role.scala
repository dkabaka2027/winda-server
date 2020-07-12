package co.winda.models

import java.time.LocalDateTime

case class Role(id: Option[Long], name: String, description: String, tenant: Boolean, permissions: List[String],
                created: Option[LocalDateTime], modified: Option[LocalDateTime])
