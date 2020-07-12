package co.winda.android

import co.winda.models.{Game, Player, Team}

case class GameItemResponse(game: Game, players: Seq[Player], head2Heads: Seq[Head2Head], teams: Seq[Team])