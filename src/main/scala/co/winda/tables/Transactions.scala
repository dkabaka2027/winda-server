package co.winda.tables

import java.time.LocalDateTime

import co.winda.Tables.{methods, orders, subscriptions, users}
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.models.Transaction

class Transactions(tag: Tag) extends Table[Transaction](tag, "transactions") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def reference = column[String]("reference")
  def amount = column[BigDecimal]("amount")
  def timestamp = column[LocalDateTime]("timestamp")
  def status = column[String]("status")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def userId = column[Long]("userId")
  def methodId = column[Long]("methodId")
  def orderId = column[Long]("orderId")

  def * = (id.?, reference, amount, timestamp, status, created, modified, userId, methodId, orderId) <> (Transaction.tupled, Transaction.unapply)

  def user = foreignKey("TRANSACTION_USER", userId, users)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def order = foreignKey("TRANSACTION_ORDER", orderId, orders)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
  def method = foreignKey("TRANSACTION_METHOD", methodId, methods)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)
}