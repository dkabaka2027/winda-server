package co.winda

import co.winda.tables._
import slick.lifted.TableQuery
import co.winda.common.slick.driver.PostgresGeoDriver.api._

object Tables {

  object countries extends TableQuery(new Countries(_))
  object games extends TableQuery(new Games(_))
  object gameEvents extends TableQuery(new GameEvents(_))
  object gameStatistics extends TableQuery(new GameStatistics(_))
  object jobs extends TableQuery(new Jobs(_))
  object leagues extends TableQuery(new Leagues(_))
  object methods extends TableQuery(new Methods(_))
  object orders extends TableQuery(new Orders(_))
  object players extends TableQuery(new Players(_))
  object playerSquads extends TableQuery(new PlayerSquads(_))
  object playerStatistics extends TableQuery(new PlayerStatistics(_))
  object playerTeams extends TableQuery(new PlayerTeams(_))
  object roles extends TableQuery(new Roles(_))
  object seasons extends TableQuery(new Seasons(_))
  object squads extends TableQuery(new Squads(_))
  object stadiums extends TableQuery(new Stadiums(_))
  object subscriptions extends TableQuery(new Subscriptions(_))
  object teams extends TableQuery(new Teams(_))
  object tokens extends TableQuery(new Tokens(_))
  object transactions extends TableQuery(new Transactions(_))
  object news extends TableQuery(new Newses(_))
  object users extends TableQuery(new Users(_))

}
