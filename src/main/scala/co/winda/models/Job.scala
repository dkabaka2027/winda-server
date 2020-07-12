package co.winda.models

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseEntity
import co.winda.crawler.models.Extractor

case class Job(id: Option[Long], name: String, description: String, urls: List[String], extractors: List[Extractor], waitFor: String,
               depth: Int, urlRegex: String, spider: Boolean, created: Option[LocalDateTime], modified: Option[LocalDateTime],
               userId: Long) extends BaseEntity[Long]
