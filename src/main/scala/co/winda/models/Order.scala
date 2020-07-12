package co.winda.models

import java.time.LocalDateTime
import co.winda.common.slick.base.BaseEntity

case class Order(id: Option[Long], reference: String, total: BigDecimal, created: Option[LocalDateTime],
                 modified: Option[LocalDateTime], subscriptionId: Long, userId: Long) extends BaseEntity[Long]
