package co.winda.models

import java.time.LocalDateTime

case class Permission(id: Option[Long], model: String, create: Boolean, read: Boolean, update: Boolean, delete: Boolean,
                      owner: Boolean, created: Option[LocalDateTime], modified: Option[LocalDateTime], roleId: Long)
