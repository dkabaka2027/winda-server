package co.winda.models

import java.time.LocalDateTime

import co.winda.common.security.enums.Algorithm

case class User(id: Option[Long], firstName: String, lastName: String, fullName: String, username: String, email: String,
                telephone: String, password: String, salt: String, algorithm: Algorithm.Value, status: String, created: Option[LocalDateTime],
                modified: Option[LocalDateTime], roleId: Long)