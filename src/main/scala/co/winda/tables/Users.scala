package co.winda.tables

import akka.pattern._
import java.time.LocalDateTime

import co.winda.Tables.roles
import co.winda.models.User
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.common.slick.base.BaseTable
import co.winda.common.security.enums.Algorithm

/**
* @author David Karigithu
* @since 21-09-2016
*/
class Users(tag: Tag) extends Table[User](tag, "users") with BaseTable[Long] {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def firstName = column[String]("firstName")
  def lastName = column[String]("lastName")
  def fullName = column[String]("fullName")
  def username = column[String]("username")
  def email = column[String]("email")
  def telephone = column[String]("telephone")
  def password = column[String]("password")
  def salt = column[String]("salt")
  def algorithm = column[Algorithm.Value]("algorithm")
  def status = column[String]("status")
  def created = column[Option[LocalDateTime]]("created", O.SqlType("timestamp default now()"))
  def modified = column[Option[LocalDateTime]]("modified", O.SqlType("timestamp default now()"))
  def roleId = column[Long]("roleId")

  def * = (id.?, firstName, lastName, fullName, username, email, telephone, password, salt, algorithm,
    status, created, modified, roleId) <> (User.tupled, User.unapply)

  def role = foreignKey("USER_ROLE", roleId, roles)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Restrict)

}
