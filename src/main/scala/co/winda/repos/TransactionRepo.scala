package co.winda.repos

import co.winda.models.{Team, Transaction}
import co.winda.tables.{Teams, Transactions}
import io.strongtyped.active.slick.Lens._
import io.strongtyped.active.slick._
import slick.ast.BaseTypedType

import scala.language.postfixOps


object TransactionRepo extends EntityActions with PostgresProfileProvider {

  import co.winda.common.slick.driver.PostgresGeoDriver.api._ //
  val baseTypedType = implicitly[BaseTypedType[Id]] //

  type Entity = Transaction //
  type Id = Long //
  type EntityTable = Transactions //

  val tableQuery = TableQuery[Transactions] //

  def $id(table: Transactions): Rep[Id] = table.id //

  val idLens = lens { transaction: Transaction => transaction.id  } //
  { (transaction, id) => transaction.copy(id = id) }

}

