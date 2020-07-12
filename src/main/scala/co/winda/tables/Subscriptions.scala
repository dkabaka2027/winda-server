package co.winda.tables

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.models.Subscription

class Subscriptions(tag: Tag) extends Table[Subscription](tag, "subscriptions") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def description = column[String]("description")
  def code = column[String]("code")
  def cost = column[BigDecimal]("cost")
  def duration = column[Int]("duration")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))

  def * = (id.?, name, description, code, cost, duration, created, modified) <> (Subscription.tupled, Subscription.unapply)

}
