package co.winda.repos

import co.winda.models.Stadium
import co.winda.tables.Stadiums
import io.strongtyped.active.slick._
import slick.ast.BaseTypedType
import io.strongtyped.active.slick.Lens._

import scala.language.postfixOps

object StadiumRepo extends EntityActions with PostgresProfileProvider {

  import co.winda.common.slick.driver.PostgresGeoDriver.api._ //
  val baseTypedType = implicitly[BaseTypedType[Id]] //

  type Entity = Stadium //
  type Id = Long //
  type EntityTable = Stadiums //

  val tableQuery = TableQuery[Stadiums] //

  def $id(table: Stadiums): Rep[Id] = table.id //

  val idLens = lens { stadium: Stadium => stadium.id  } //
  { (stadium, id) => stadium.copy(id = id) }

}


