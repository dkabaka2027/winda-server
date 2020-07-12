package co.winda.predictive.data

import java.io.IOException
import java.util

import akka.util.Timeout
import co.winda.common.slick.driver.PostgresGeoDriver
import com.typesafe.config.Config
import org.deeplearning4j.arbiter.optimize.api.data.DataProvider
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.nd4j.linalg.dataset.api.iterator.MultipleEpochsIterator
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher

import scala.concurrent.ExecutionContext

class WindaDataProvider(
   implicit val config: Config,
   implicit val db: PostgresGeoDriver#Backend#Database,
   implicit val profile: PostgresGeoDriver,
   implicit val ec: ExecutionContext,
   implicit val timeout: Timeout
) extends DataProvider {
  private var numEpochs = 0
  private var batchSize = 0

  def apply(numEpochs: Int, batchSize: Int): WindaDataProvider = {
    this.numEpochs = numEpochs
    this.batchSize = batchSize
    this
  }

  override def trainData(dataParameters: util.Map[String, AnyRef]): AnyRef = {
    try {
      return new MultipleEpochsIterator(numEpochs, new WindaDataSetIterator(batchSize, 1000000, new WindaDataFetcher))
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    }
  }

  override def testData(dataParameters: util.Map[String, AnyRef]): AnyRef = {
    try {
      return new WindaDataSetIterator(batchSize, 1000000, new WindaDataFetcher)
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    }
  }

  override def getDataType: Class[_] = classOf[WindaDataSetIterator]
}