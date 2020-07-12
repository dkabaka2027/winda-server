package co.winda.predictive.models

import co.winda.tables.Games
import co.winda.models.{Game, Player, Team}
import org.nd4j.linalg.api.ndarray.INDArray

case class WindaInput(game: Game, homeTeam: Team, awayTeam: Team, homeGames: Seq[Game], awayGames: Seq[Game],
                      homePlayers: List[Player], awayPlayers: List[Player]) {

  def input: INDArray = ???

}
