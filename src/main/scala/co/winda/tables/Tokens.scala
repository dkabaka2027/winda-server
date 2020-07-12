package co.winda.tables

import akka.pattern._
import java.time.LocalDateTime

import co.winda.Tables.users
import co.winda.models.Token
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver.api._

/**
* @author David Karigithu
* @since 21-09-2016
*/
class Tokens(tag: Tag) extends Table[Token](tag, "tokens") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def token = column[String]("token")
  def ttl = column[Int]("ttl")
  def active = column[Boolean]("active")
  def created = column[Option[LocalDateTime]]("created")
  def ownedBy = column[Long]("ownedBy")

  def * = (id.?, token, ttl, active, created, ownedBy) <> (Token.tupled, Token.unapply)

  def user = foreignKey("TOKEN_USER", ownedBy, users)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)

}