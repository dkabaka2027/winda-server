package co.winda.models

import java.time.LocalDateTime

case class News(id: Option[Long], title: String, content: String, excerpt: String, image: String, created: Option[LocalDateTime],
                modified: Option[LocalDateTime])
