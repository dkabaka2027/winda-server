package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.users
import co.winda.common.slick.base.BaseTable
import co.winda.models.Job
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.crawler.models.Extractor

class Jobs(tag: Tag) extends Table[Job](tag,"jobs") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def description = column[String]("description")
  def urls = column[List[String]]("urls")
  def extractors = column[List[Extractor]]("extractors")
  def waitFor = column[String]("waitFor")
  def depth = column[Int]("depth")
  def urlRegex = column[String]("urlRegex")
  def spider = column[Boolean]("spider")
  def lastRun = column[LocalDateTime]("lastRun")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def userId = column[Long]("userId")

  def * = (id.?, name, description, urls, extractors, waitFor, depth, urlRegex, spider, created, modified, userId) <> (Job.tupled, Job.unapply)

  def user = foreignKey("JOB_USER", userId, users)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}
