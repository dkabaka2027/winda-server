package co.winda.tables

import java.time.LocalDateTime

import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.models.Order

class Orders(tag: Tag) extends Table[Order](tag, "orders") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def reference = column[String]("reference")
  def total = column[BigDecimal]("total")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def subscriptionId = column[Long]("subscriptionId")
  def userId = column[Long]("userId")

  def * =  (id.?, reference, total, created, modified, subscriptionId, userId) <> (Order.tupled, Order.unapply)
}
