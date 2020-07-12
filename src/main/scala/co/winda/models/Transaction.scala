package co.winda.models

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseEntity

case class Transaction(id: Option[Long], reference: String, amount: BigDecimal, timestamp: LocalDateTime, status: String,
                       created: Option[LocalDateTime], modified: Option[LocalDateTime], userId: Long, methodId: Long,
                       orderId: Long) extends BaseEntity[Long]
