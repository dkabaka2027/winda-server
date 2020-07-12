package co.winda.repos

import co.winda.models.{Team, User}
import co.winda.tables.{Teams, Users}
import io.strongtyped.active.slick.Lens._
import io.strongtyped.active.slick._
import slick.ast.BaseTypedType

import scala.language.postfixOps


object UserRepo extends EntityActions with PostgresProfileProvider {

  import co.winda.common.slick.driver.PostgresGeoDriver.api._ //
  val baseTypedType = implicitly[BaseTypedType[Id]] //

  type Entity = User //
  type Id = Long //
  type EntityTable = Users //

  val tableQuery = TableQuery[Users] //

  def $id(table: Users): Rep[Id] = table.id //

  val idLens = lens { user: User => user.id  } //
  { (user, id) => user.copy(id = id) }

}

