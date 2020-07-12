package co.winda.predictive

import java.io.{File, IOException}
import java.time.LocalDateTime
import java.util
import java.util.Properties

import co.winda.common.slick.base.BaseEntity
import org.deeplearning4j.arbiter.ComputationGraphSpace
import org.deeplearning4j.arbiter.conf.updater.SgdSpace
import org.deeplearning4j.arbiter.optimize.parameter.continuous.ContinuousParameterSpace
import org.deeplearning4j.ui.api.UIServer
import org.deeplearning4j.arbiter.layers.{ConvolutionLayerSpace, DenseLayerSpace, OutputLayerSpace}
import org.deeplearning4j.arbiter.optimize.generator.GridSearchCandidateGenerator
import org.deeplearning4j.arbiter.optimize.parameter.discrete.DiscreteParameterSpace
import org.deeplearning4j.arbiter.optimize.parameter.integer.IntegerParameterSpace
import org.deeplearning4j.arbiter.saver.local.FileModelSaver
import org.deeplearning4j.arbiter.scoring.impl.EvaluationScoreFunction
import org.deeplearning4j.core.storage.StatsStorage
import org.deeplearning4j.arbiter.optimize.api.OptimizationResult
import org.deeplearning4j.arbiter.optimize.api.saving.ResultReference
import org.deeplearning4j.arbiter.optimize.api.saving.ResultSaver
import org.deeplearning4j.arbiter.optimize.api.score.ScoreFunction
import org.deeplearning4j.arbiter.optimize.api.termination.MaxCandidatesCondition
import org.deeplearning4j.arbiter.optimize.api.termination.MaxTimeCondition
import org.deeplearning4j.arbiter.optimize.api.termination.TerminationCondition
import org.deeplearning4j.arbiter.optimize.config.OptimizationConfiguration
import org.deeplearning4j.arbiter.optimize.runner.IOptimizationRunner
import org.deeplearning4j.arbiter.optimize.runner.LocalOptimizationRunner
import org.deeplearning4j.arbiter.saver.local.FileModelSaver
import org.deeplearning4j.arbiter.scoring.impl.EvaluationScoreFunction
import org.deeplearning4j.arbiter.task.MultiLayerNetworkTaskCreator
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.ui.api.UIServer
import java.util.concurrent.TimeUnit

import akka.actor.{ActorContext, ActorSystem}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import co.winda.common.slick.driver.PostgresGeoDriver
import co.winda.predictive.data.WindaDataProvider
import co.winda.predictive.models.WindaInput
import com.typesafe.config.Config
import org.deeplearning4j.arbiter.data.MnistDataProvider
import org.deeplearning4j.arbiter.optimize.api.data.{DataProvider, DataSource}
import org.deeplearning4j.arbiter.optimize.runner.listener.StatusListener
import org.deeplearning4j.arbiter.optimize.runner.listener.impl.LoggingStatusListener
import org.deeplearning4j.datasets.iterator.{BaseDatasetIterator, DataSetFetcher, MultipleEpochsIterator}
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.stepfunctions.{GradientStepFunction, StepFunction}
import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.ui.VertxUIServer
import org.deeplearning4j.ui.model.storage.{FileStatsStorage, InMemoryStatsStorage}
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher

import scala.concurrent.ExecutionContext

class Trainer(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val config: Config,
  implicit val db: PostgresGeoDriver#Backend#Database,
  implicit val profile: PostgresGeoDriver,
  implicit val ec: ExecutionContext,
  implicit val timeout: Timeout
) {

  def apply() = {
    init()
    this
  }

  def init(): Unit = {
    //Initialize the user interface backend
    val uiServer: UIServer = VertxUIServer.getInstance()

    //Configure where the network information (gradients, activations, score vs. time etc) is to be stored//Configure where the network information (gradients, activations, score vs. time etc) is to be stored
    //Then add the StatsListener to collect this information from the network, as it trains
    val statsStorage = new InMemoryStatsStorage
    //Alternative: new FileStatsStorage(File) - see UIStorageExample
    val listenerFrequency = 1
    // computationGraph.setListeners(new StatsListener(statsStorage, listenerFrequency))

    //  Computation Graph
    val computationGraph: ComputationGraphSpace = new ComputationGraphSpace.Builder()
      .updater(new SgdSpace(new ContinuousParameterSpace(0, 1)))
      .l1(new ContinuousParameterSpace(0, 1))
      .l2(new ContinuousParameterSpace(0, 1))
      .stepFunction(new GradientStepFunction())
      .biasInit(new ContinuousParameterSpace(0, 1))
      .dropOut(new ContinuousParameterSpace(0, 1))
      .optimizationAlgo(OptimizationAlgorithm.LBFGS)
      .addInputs("homeRating")
      .addInputs("awayRating")
      .addInputs("homeStreak")
      .addInputs("awayStreak")
      .setOutputs("homeWin", "draw", "awayWin")
      .addLayer("input", new DenseLayerSpace.Builder()
        .activation(Activation.RELU)
        .dropOut(new ContinuousParameterSpace(0, 1))
        .nIn(6)
        .nOut(new IntegerParameterSpace(32, 512))
        .build()
      )
      .addLayer("convolutional", new ConvolutionLayerSpace.Builder()
        .activation(Activation.RELU)
        .dropOut(new ContinuousParameterSpace(0, 1))
        .nIn(new IntegerParameterSpace(32, 512))
        .nOut(new IntegerParameterSpace(32, 512))
        .build()
      )
      .addLayer("output", new OutputLayerSpace.Builder()
        .activation(Activation.RELU)
        .dropOut(new ContinuousParameterSpace(0, 1))
        .nIn(new IntegerParameterSpace(32, 512))
        .nOut(3)
        .build()
      )
      .build()

    //Now: We need to define a few configuration options
    // (a) How are we going to generate candidates? (random search or grid search)
    // TODO: Write up a GA Candidate Generator
    val candidateGenerator = new GridSearchCandidateGenerator(
      computationGraph,
      4,
      GridSearchCandidateGenerator.Mode.Sequential,
      new java.util.HashMap()
    )

    // (b) How are going to provide data? We'll use a simple data source that returns MNIST data// (b) How are going to provide data? We'll use a simple data source that returns MNIST data
    // Note that we set teh number of epochs in MultiLayerSpace above
    val dataProvider = new WindaDataProvider()

    // (c) How we are going to save the models that are generated and tested?
    //     In this example, let's save them to disk the working directory
    //     This will result in examples being saved to arbiterExample/0/, arbiterExample/1/, arbiterExample/2/, ...
    val baseSaveDirectory = "arbiterExample/"
    val f = new File(baseSaveDirectory)
    if (f.exists) f.delete
    f.mkdir
    val modelSaver = new FileModelSaver(baseSaveDirectory)

    // (d) What are we actually trying to optimize?
    //     In this example, let's use classification accuracy on the test set
    //     See also ScoreFunctions.testSetF1(), ScoreFunctions.testSetRegression(regressionValue) etc
    val scoreFunction = new EvaluationScoreFunction(Evaluation.Metric.ACCURACY)

    // (e) When should we stop searching? Specify this with termination conditions
    //     For this example, we are stopping the search at 15 minutes or 10 candidates - whichever comes first
    val terminationConditions = new java.util.ArrayList[TerminationCondition]()
    terminationConditions.add(new MaxTimeCondition(15, TimeUnit.DAYS))
    terminationConditions.add(new MaxCandidatesCondition(1000000))

    //Given these configuration options, let's put them all together:
    val configuration: OptimizationConfiguration = new OptimizationConfiguration.Builder()
      .candidateGenerator(candidateGenerator)
      .dataProvider(dataProvider)
      .modelSaver(modelSaver)
      .scoreFunction(scoreFunction)
      .terminationConditions(terminationConditions)
      .build()

    //And set up execution locally on this machine:
    val runner = new LocalOptimizationRunner(configuration, new MultiLayerNetworkTaskCreator)

    //Start the UI. Arbiter uses the same storage and persistence approach as DL4J's UI
    //Access at http://localhost:9000/arbiter
    val ss = new FileStatsStorage(new File("arbiterExampleUiStats.dl4j"))
    //  runner.addListeners(new LoggingStatusListener(ss))
    uiServer.attach(ss)

    //Start the hyperparameter optimization
    runner.execute()

    //Print out some basic stats regarding the optimization procedure
    val s: String = "Best score: " + runner.bestScore + "\n" + "Index of model with best score: " + runner.bestScoreCandidateIndex + "\n" + "Number of configurations evaluated: " + runner.numCandidatesCompleted + "\n"
    System.out.println(s)

    //Get all results, and print out details of the best result:
    val indexOfBestResult: Int = runner.bestScoreCandidateIndex
    val allResults: java.util.List[ResultReference] = runner.getResults

    val bestResult: OptimizationResult = allResults.get(indexOfBestResult).getResult
    val bestModel: MultiLayerNetwork = bestResult.getResultReference.getResultModel.asInstanceOf[MultiLayerNetwork]

    System.out.println("\n\nConfiguration of best model:\n")
    System.out.println(bestModel.getLayerWiseConfigurations.toJson)

    // Wait a while before exiting
    Thread.sleep(60000)
    VertxUIServer.stopInstance()
  }

}