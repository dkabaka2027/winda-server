package co.winda.models

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseEntity

case class Subscription(id: Option[Long], name: String, description: String, code: String, cost: BigDecimal, duration: Int,
                        created: Option[LocalDateTime], modified: Option[LocalDateTime]) extends BaseEntity[Long]
