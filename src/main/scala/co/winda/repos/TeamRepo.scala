package co.winda.repos

import co.winda.models.Team
import co.winda.tables.Teams
import io.strongtyped.active.slick._
import slick.ast.BaseTypedType
import io.strongtyped.active.slick.Lens._

import scala.language.postfixOps


object TeamRepo extends EntityActions with PostgresProfileProvider {

  import co.winda.common.slick.driver.PostgresGeoDriver.api._ //
  val baseTypedType = implicitly[BaseTypedType[Id]] //

  type Entity = Team //
  type Id = Long //
  type EntityTable = Teams //

  val tableQuery = TableQuery[Teams] //

  def $id(table: Teams): Rep[Id] = table.id //

  val idLens = lens { team: Team => team.id  } //
  { (team, id) => team.copy(id = id) }

}

