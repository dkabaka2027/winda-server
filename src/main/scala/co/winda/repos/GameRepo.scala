package co.winda.repos

import co.winda.models.Game
import co.winda.tables.Games
import io.strongtyped.active.slick._
import slick.ast.BaseTypedType
import io.strongtyped.active.slick.Lens._

import scala.language.postfixOps


object GameRepo extends EntityActions with PostgresProfileProvider {

  import co.winda.common.slick.driver.PostgresGeoDriver.api._ //
  val baseTypedType = implicitly[BaseTypedType[Id]] //

  type Entity = Game //
  type Id = Long //
  type EntityTable = Games //

  val tableQuery = TableQuery[Games] //

  def $id(table: Games): Rep[Id] = table.id //

  val idLens = lens { game: Game => game.id  } //
  { (game, id) => game.copy(id = id) }

}
