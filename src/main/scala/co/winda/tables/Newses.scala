package co.winda.tables

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.models.{News}

class Newses(tag: Tag) extends Table[News](tag, "newses") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def title = column[String]("title")
  def content = column[String]("content")
  def excerpt = column[String]("excerpt")
  def image = column[String]("image")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))

  def * =  (id.?, title, content, excerpt, image, created, modified) <> (News.tupled, News.unapply)
}
