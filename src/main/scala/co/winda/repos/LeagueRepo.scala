package co.winda.repos

import co.winda.models.League
import co.winda.tables.Leagues
import io.strongtyped.active.slick._
import slick.ast.BaseTypedType
import io.strongtyped.active.slick.Lens._

import scala.language.postfixOps


object LeagueRepo extends EntityActions with PostgresProfileProvider {

  import co.winda.common.slick.driver.PostgresGeoDriver.api._ //
  val baseTypedType = implicitly[BaseTypedType[Id]] //

  type Entity = League //
  type Id = Long //
  type EntityTable = Leagues //

  val tableQuery = TableQuery[Leagues] //

  def $id(table: Leagues): Rep[Id] = table.id //

  val idLens = lens { league: League => league.id  } //
  { (league, id) => league.copy(id = id) }

}

