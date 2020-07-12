package co.winda.android

import co.winda.models.{Game, League, Season, Team}

case class LeagueResponse(league: League, season: Season, items: Seq[LeagueItem])
