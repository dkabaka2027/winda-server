package co.winda.repos

import co.winda.models.Player
import co.winda.tables.Players
import io.strongtyped.active.slick._
import slick.ast.BaseTypedType
import io.strongtyped.active.slick.Lens._

import scala.language.postfixOps


object PlayerRepo extends EntityActions with PostgresProfileProvider {

  import co.winda.common.slick.driver.PostgresGeoDriver.api._ //
  val baseTypedType = implicitly[BaseTypedType[Id]] //

  type Entity = Player //
  type Id = Long //
  type EntityTable = Players //

  val tableQuery = TableQuery[Players] //

  def $id(table: Players): Rep[Id] = table.id //

  val idLens = lens { player: Player => player.id  } //
  { (player, id) => player.copy(id = id) }

}
