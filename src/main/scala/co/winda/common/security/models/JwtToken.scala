package co.winda.common.security.models

import java.time.LocalDateTime

import co.winda.models.Role
import co.winda.models.User

/**
* @author David Karigithu
* @since 27-09-2016
*/
case class JWT(token: String, ttl: BigInt, created: LocalDateTime)
case class JwtToken(token: JWT, user: User)