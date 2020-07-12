package co.winda.predictive.data

import java.nio.LongBuffer
import java.util

import akka.actor.{ActorContext, ActorSystem}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import co.winda.Tables._
import co.winda.common.slick.driver.PostgresGeoDriver
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.common.slick.extensions.QueryExtensions._
import co.winda.models._
import com.google.flatbuffers.FlatBufferBuilder
import com.typesafe.config.Config
import org.nd4j.linalg.api.blas.params.MMulTranspose
import org.nd4j.linalg.api.buffer.DataBuffer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.cpu.nativecpu.NDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.conditions.Condition
import org.nd4j.linalg.indexing.INDArrayIndex

import scala.concurrent.ExecutionContext
import scala.util.Success

class WindaDataFetcher(
  implicit val config: Config,
  implicit val db: PostgresGeoDriver#Backend#Database,
  implicit val profile: PostgresGeoDriver,
  implicit val ec: ExecutionContext,
  implicit val timeout: Timeout
) extends BaseDataFetcher {

  def fetch(numExamples: Int): Unit = {
    if (!hasMore) throw new IllegalStateException("Unable to get more; there are no more images")

    val action = for {
      gs <- games.page(this.cursor, numExamples).sortBy(_.play).result.transactionally
      gStats <- gameStatistics.filter(s => s.teamId.inSet(gs.foldRight(Seq[Long]())((a, b) => b ++ Seq(a.homeTeamId, a.awayTeamId))) && s.gameId.inSet(gs.foldRight(Seq[Long]())((a, b) => b ++ Seq(a.id.get)))).result.transactionally
      ts <- teams.filter(_.id.inSet(gs.foldRight(Seq[Long]())((a, b) => b ++ Seq(a.homeTeamId, a.awayTeamId)))).result.transactionally
      ss <- squads.filter(_.gameId.inSet(gs.foldRight(Seq[Long]())((a, b) => b ++ Seq(a.homeTeamId, a.awayTeamId)))).result.transactionally
      playerS <- playerSquads.filter(_.squadId.inSet(ss.foldRight(Seq[Long]())((a, b) => b ++ Seq(a.id.get)))).result.transactionally
      ps <- players.filter(_.id.inSet(playerS.foldRight(Seq[Long]())((a, b) => b ++ Seq(a.playerId)))).result.transactionally
      pStats <- playerStatistics.filter(_.id.inSet(playerS.foldRight(Seq[Long]())((a, b) => b ++ Seq(a.playerId)))).result.transactionally
    } yield gs.map { g =>
      val squad = ss.find(_.gameId == g.id.get).get
      val playerSquad = playerS.find(_.squadId == squad.id.get).get
      val home6NN = gs.take(6).filter(a => (a.play isBefore g.play) && (a.homeTeamId == g.homeTeamId || a.awayTeamId == g.homeTeamId))
      val away6NN = gs.take(6).filter(a => (a.play isBefore g.play) && (a.homeTeamId == g.awayTeamId || a.awayTeamId == g.awayTeamId))
      (
        (g, gStats.filter(_.gameId == g.id.get)),
        ts.filter(t => Seq(g.homeTeamId, g.awayTeamId).contains(t.id.get)),
        home6NN.map(h => (h, gStats.filter(_.gameId == h.id.get))),
        home6NN.map(a => (a, gStats.filter(_.gameId == a.id.get))),
        squad,
        playerSquad,
        ps.filter(_.id.get == playerSquad.id.get).map(p => (p, pStats.filter(_.playerId == p.id.get)))
      )
    }

    /**
    * Feature Set
    * | 6NN Past Performance Home & Away                       |
    * | Away Player Rating                                     |
    * | Home Player Rating                                     |
    * | League Statistics: Index, Played, Won, Drawn, Lost, GD |
    *
    * Output:
    * | Home Win                 Draw                 Away Win |
    *
    */
    db.run(action).andThen { case Success(res) =>
      // TODO: Convert the res tuple into a feature and label NDArray
      // TODO: Add Game Statistics
      val dataSet: (Seq[NDArray], Seq[NDArray]) = res.map { r: ((Game, Seq[GameStatistic]), Seq[Team], Seq[(Game, Seq[GameStatistic])], Seq[(Game, Seq[GameStatistic])], Squad, PlayerSquad, Seq[(Player, Seq[PlayerStatistic])]) =>
        // TODO: Loop through games and compute an index based on game statistic for: Defense, Attack, Counter, Creativity,
        // Home 6NN
        val home6NN: Array[Double] = r._3.foldRight(Seq[Seq[Double]]()) { (a, b) =>
          b
        }.foldRight(Array[Double]()) { (a, b) =>
          b.+:(a.sum / a.length)
        }
        // Away 6NN
        val away6NN: Array[Double] = r._4.foldRight(Seq[Seq[Double]]()) { (a, b) =>
          b
        }.foldRight(Array[Double]()) { (a, b) =>
          b.+:(a.sum / a.length)
        }

        // Home Player Stats
        val homePlayer = r._7.filter(_._1.teamId == r._1._1.homeTeamId).map { p =>
          ((p._2.head.pace + p._2.head.defense + p._2.head.passing + p._2.head.dribbling + p._2.head.shooting + p._2.head.physical)/6).toDouble
        }.toArray
        // Away Player Stats
        val awayPlayer = r._7.filter(_._1.teamId == r._1._1.awayTeamId).map { p =>
          ((p._2.head.pace + p._2.head.defense + p._2.head.passing + p._2.head.dribbling + p._2.head.shooting + p._2.head.physical)/6).toDouble
        }.toArray

        // TODO: Filter Games by Season then accumulate statistics: Index, Played, Won, Drawn, Lost, GD
        // TODO: All games in a Seasons may be out of set, figure out a way to get them in
        // Home League Stats
        val homeLeague = Array[Double]()

        // Away League Stats
        val awayLeague = Array[Double]()

        // TODO: For each game examine outcome and assign array as: HomeWin = |1, 0, 0|, Draw = |0, 1, 0|, AwayWin = |0, 0, 1|
        // Output
        val output: NDArray = {
          if (r._1._1.homeScore.get > r._1._1.awayScore.get) new NDArray(Array(Array[Double](1,0,0)))
          if (r._1._1.homeScore.get < r._1._1.awayScore.get) new NDArray(Array(Array[Double](0,1,0)))
          if (r._1._1.homeScore.get == r._1._1.awayScore.get) new NDArray(Array(Array[Double](0,0,1)))
          new NDArray(Array(Array[Double](1,0,0)))
        }

        val featureSet: Array[Array[Double]] = Array(home6NN.++(away6NN), homePlayer, awayPlayer, homeLeague, awayLeague)
        val features: NDArray = new NDArray(featureSet)

        (features, output)
      }.foldRight((Seq[NDArray](), Seq[NDArray]())) { (a, b) =>
        (b._1.+:(a._1), b._2.+:(a._2))
      }

      this.curr = new DataSet(Nd4j.vstack(dataSet._1:_*), Nd4j.vstack(dataSet._2:_*))
    }


  }

}
