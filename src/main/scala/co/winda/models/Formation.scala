package co.winda.models

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseEntity

case class Position(name: String, code: String, x: Float, y: Float) extends Serializable

case class Formation(id: Option[Long], name: String, description: String, code: String, positions: List[Position],
                     created: Option[LocalDateTime], modified: Option[LocalDateTime]) extends BaseEntity[Long]
