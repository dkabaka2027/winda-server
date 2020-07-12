package co.winda.predictive

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import co.winda.common.slick.driver.PostgresGeoDriver
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.predictive.models.WindaInput
import com.typesafe.config.Config
import org.deeplearning4j.nn.graph.ComputationGraph
import org.nd4j.linalg.api.ndarray.INDArray
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext

class Network(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val config: Config,
  implicit val db: PostgresGeoDriver#Backend#Database,
  implicit val profile: PostgresGeoDriver,
  implicit val ec: ExecutionContext,
  implicit val timeout: Timeout
) {

  var network: ComputationGraph = ComputationGraph.load(new File(""), true)

  def apply(): Network = {
    new Network()
  }

  def test(input: INDArray): INDArray = {
    network.outputSingle(input)
  }

}
