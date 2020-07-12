package co.winda

import co.winda.common.modules.DBModule
import co.winda.common.slick.driver.PostgresGeoDriver
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import slick.migration.api.{PostgresDialect, ReversibleMigrationSeq, TableMigration}

import scala.concurrent.ExecutionContext

object Migrations {
  implicit val dialect = new PostgresDialect

  def init = {
    val countries = TableMigration(Tables.countries)
      .addForeignKeys(_.parentKey)
      .addColumns(_.id, _.name, _.description, _.code, _.`type`, _.created, _.modified, _.parent)
      .create

    val games = TableMigration(Tables.games)
      .addColumns(_.id, _.key, _.homeTeamId, _.awayTeamId, _.play, _.`type`, _.status, _.created, _.modified, _.seasonId)
      .addForeignKeys(_.homeTeam, _.awayTeam, _.season)
      .create

    val gameEvents = TableMigration(Tables.gameEvents)
      .addColumns(_.id, _.`type`, _.timestamp, _.quadrant, _.created, _.modified, _.gameId)
      .addForeignKeys(_.game)
      .create

    val gameStatistics = TableMigration(Tables.gameStatistics)
      .addColumns(_.id, _.created, _.modified)
      .create

    val leagues = TableMigration(Tables.leagues)
      .addColumns(_.id, _.name, _.description, _.icon, _.key, _.club, _.created, _.modified)
      .addForeignKeys(_.country)
      .create

    val players = TableMigration(Tables.players)
      .addColumns(_.id, _.name, _.bio, _.image, _.position, _.created, _.modified, _.internationalTeamId, _.teamId)
      .addForeignKeys(_.international, _.team)
      .create

    val playerSquads = TableMigration(Tables.playerSquads)
      .addColumns(_.id, _.starting, _.created, _.modified, _.currentStat, _.playerId, _.squadId)
      .addForeignKeys(_.player, _.squad)
      .create

    val playerStatistics = TableMigration(Tables.playerStatistics)
      .addColumns(_.id, _.pace, _.shooting, _.dribbling, _.defense, _.physical, _.created, _.modified, _.playerId)
      .create

    val playerTeams = TableMigration(Tables.playerTeams)
      .addColumns(_.id, _.buyingPrice, _.weeklySalary, _.from, _.to, _.created, _.modified, _.currentStat, _.playerId, _.teamId)
      .addForeignKeys(_.player, _.team)
      .create

    val roles = TableMigration(Tables.roles)
      .addColumns(_.id, _.name, _.description, _.tenant, _.permissions, _.created, _.modified)
      .create

    val seasons = TableMigration(Tables.seasons)
      .addColumns(_.id, _.start, _.end, _.created, _.modified, _.countryId, _.leagueId)
      .addForeignKeys(_.country, _.league)
      .create

    val squads = TableMigration(Tables.squads)
      .addColumns(_.id, _.created, _.modified, _.teamId, _.gameId)
      .addForeignKeys(_.game, _.team)
      .create

    val stadiums = TableMigration(Tables.stadiums)
      .addColumns(_.id, _.name, _.description, _.location, _.created, _.modified, _.teamId, _.countryId)
      .addForeignKeys(_.country, _.team)
      .create

    val subscriptions = TableMigration(Tables.subscriptions)
      .addColumns(_.id, _.name, _.description, _.code, _.cost, _.created, _.modified)
      .create

    val teams = TableMigration(Tables.teams)
      .addColumns(_.id, _.key, _.title, _.altTitle, _.history, _.code, _.icon, _.synonyms, _.club, _.since, _.address,
        _.web, _.created, _.modified, _.leagueId, _.cityId, _.countryId)
      .addForeignKeys(_.city, _.country, _.league)
      .create

    val tokens = TableMigration(Tables.tokens)
      .addColumns(_.id, _.token, _.ttl, _.active, _.created, _.ownedBy)
      .addForeignKeys(_.user)
      .create

    val transactions = TableMigration(Tables.transactions)
      .addColumns(_.id, _.reference, _.amount, _.timestamp, _.status, _.created, _.modified, _.userId, _.methodId)
      .addForeignKeys(_.user, _.method)
      .create

    val users = TableMigration(Tables.users)
      .addColumns(_.id, _.firstName, _.lastName, _.fullName, _.username, _.email, _.telephone, _.password, _.salt, _.algorithm,
        _.status, _.created, _.modified, _.roleId)
      .addForeignKeys(_.role)
      .create

    // countries & games & gameEvents & gameStatistics & leagues & players & playerSquads & playerStatistics & playerTeams &
    //   roles & seasons & squads & stadiums & subscriptions & teams & tokens & transactions & users

    (Tables.countries.schema ++ Tables.games.schema ++ Tables.gameEvents.schema ++ Tables.gameStatistics.schema ++
      Tables.jobs.schema ++ Tables.leagues.schema ++ Tables.methods.schema ++ Tables.news.schema ++ Tables.orders.schema ++
      Tables.players.schema ++ Tables.playerSquads.schema ++ Tables.playerStatistics.schema ++ Tables.playerTeams.schema ++
      Tables.roles.schema ++ Tables.seasons.schema ++ Tables.squads.schema ++ Tables.stadiums.schema ++
      Tables.subscriptions.schema ++ Tables.teams.schema ++ Tables.tokens.schema ++ Tables.transactions.schema ++
      Tables.users.schema).create
  }

  def run(implicit db: PostgresGeoDriver#Backend#Database, profile: PostgresGeoDriver, ec: ExecutionContext) = {
    db.run(
      DBIO.seq(
        (Tables.countries.schema ++ Tables.games.schema ++ Tables.gameEvents.schema ++ Tables.gameStatistics.schema ++
        Tables.jobs.schema ++ Tables.leagues.schema ++ Tables.methods.schema ++ Tables.news.schema ++ Tables.orders.schema ++
        Tables.players.schema ++ Tables.playerSquads.schema ++ Tables.playerStatistics.schema ++ Tables.playerTeams.schema ++
        Tables.roles.schema ++ Tables.seasons.schema ++ Tables.squads.schema ++ Tables.stadiums.schema ++
        Tables.subscriptions.schema ++ Tables.teams.schema ++ Tables.tokens.schema ++ Tables.transactions.schema ++
        Tables.users.schema).drop,
        init
      )
    )
  }

}
