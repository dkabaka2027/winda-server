package co.winda.models

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseEntity

case class Method(id: Option[Long], name: String, description: String, code: String, created: Option[LocalDateTime],
                  modified: Option[LocalDateTime]) extends BaseEntity[Long]
