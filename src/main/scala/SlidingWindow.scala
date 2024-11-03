package org.cs441homework2.com

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.SparkConf
import org.apache.spark.api.java.function.FlatMapFunction
import org.apache.spark.api.java.{JavaRDD, JavaSparkContext}
import org.deeplearning4j.datasets.iterator.IteratorDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.util.ModelSerializer

import java.io.File
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import java.util
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.jdk.CollectionConverters._

case class WindowedData(input: Array[String], target: String) {
  def getInput: Array[String] = {
    input
  }
  def getTarget: String = {
    target
  }
}

class SlidingWindowUtils extends Serializable with LazyLogging {
  def createSlidingWindows(sentence: String, windowSize: Int): List[WindowedData]  = {
    logger.debug("Entered createSlidingWindows")

    val tokens = sentence.split(" ")

    // Create sliding windows
    Range(0, (tokens.length - windowSize) ).map { i =>
      val inputWindow = new Array[String](windowSize)
      System.arraycopy(tokens, i, inputWindow, 0, windowSize)
      val target = tokens(i + windowSize)
      WindowedData(inputWindow, target)
    }.toList

  }
}

object TransformerModel extends LazyLogging {
  def createModel(inputSize: Int, hiddenSize: Int, outputSize: Int): MultiLayerNetwork = {
    logger.debug("Entered createModel")

    // Create the model here
    val conf:MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .list()
      .layer(0, new DenseLayer.Builder().nIn(inputSize).nOut(hiddenSize)
        .weightInit(WeightInit.XAVIER)
        .activation(Activation.RELU)
        .build())
      .layer(1, new OutputLayer.Builder(LossFunction.MSE)
        .weightInit(WeightInit.XAVIER)
        .activation(Activation.SIGMOID)
        .nIn(hiddenSize).nOut(outputSize).build())
      .build()

    val model = new MultiLayerNetwork(conf)
    model
  }
}

object SlidingWindow extends LazyLogging {
  private def convertWindowDataToDataSet(windowedData: util.List[WindowedData]): List[DataSet] = {
    logger.debug("Entered convertWindowDataToDataSet")

    windowedData.map { window =>
      // Convert input string into double, then reshape to 2d instead of just 1d
      val inputVector = Nd4j.create(window.getInput.map(_.toDouble)).reshape(1, window.getInput.length)
      // Convert the coordinates from string into double, then reshape to 1d
      val targetVector = Nd4j.create(Array(window.getTarget.toDouble)).reshape(1, 1)

      new DataSet(inputVector, targetVector)
    }.toList
  }

  private def convertDataSetToRDD(data:List[DataSet], sc:JavaSparkContext): JavaRDD[DataSet] = {
    logger.debug("Entered convertDataSetToRDD")

    val rddData:JavaRDD[DataSet] = sc.parallelize(data)
    rddData
  }

  def startOfProcess(inputPath: String, outputPath: String): Unit = {
    logger.debug("Entered SlidingWindow.startOfProcess()")

    // Set up Spark configuration and context
    val conf = new SparkConf()
      .setAppName("Sliding Window Dataset")
      .setMaster("local[*]")
    val sc =  new JavaSparkContext(conf) // Read sc as SparkContext

    // textFile() reads the text from a file line by line and automatically converts data to RDD
    val sentenceRDD = sc.textFile(inputPath) // Already in RDD form

    // Apply the sliding window logic to create the dataset
    val slidingWindowUtils = new SlidingWindowUtils

    val slidingWindowDataset = sentenceRDD.flatMap(new FlatMapFunction[String, WindowedData] {
      override def call(sentence:String): util.Iterator[WindowedData] = {
        slidingWindowUtils.createSlidingWindows(sentence, 4).iterator.asJava
      }
    })

    val batchSize = 2 // Specify the batch size

    // Retrieve the data in different formats for different functions
    val convertedDataSet: List[DataSet] = convertWindowDataToDataSet(slidingWindowDataset.collect())
    val convertedDataSetIterator: util.Iterator[DataSet] = convertedDataSet.asJava.iterator()
    val rddDataSet:JavaRDD[DataSet] = convertDataSetToRDD(convertedDataSet, sc)

    // Create an instance of an iterator
    val dataSetIterator: IteratorDataSetIterator = new IteratorDataSetIterator(convertedDataSetIterator, batchSize)

    // Load or create a model
    val model: MultiLayerNetwork = TransformerModel.createModel(4, 64, 1) // Input size, hidden size, output size
    model.init() // Initialize the model

    // Train the model using the DataSetIterator
    val startTime = System.currentTimeMillis
    model.fit(dataSetIterator)
    val endTime = System.currentTimeMillis
    System.out.println("Epoch time: " + (endTime - startTime) + "ms")

    // Specify the file where the model will be saved
    val locationToSave: String = outputPath + "trained_model.zip"

    // Whether or not to save the updater (optimizer state)
    val saveUpdater: Boolean = true

    // Save the trained model to the specified file
    ModelSerializer.writeModel(model, locationToSave, saveUpdater);

    // Set up ParameterAveragingTrainingMaster
    val trainingMaster = new ParameterAveragingTrainingMaster.Builder(32)
      .batchSizePerWorker(32)  // Batch size on each Spark worker
      .averagingFrequency(5)   // Frequency of parameter averaging
      .build();

    // Create a SparkDl4jMultiLayer with the Spark context and model
    val sparkModel = new SparkDl4jMultiLayer(sc, model, trainingMaster)

    model.setListeners(new ScoreIterationListener(10))
    System.out.println("Current Learning Rate: " + model.getLearningRate(1))


    // Train the model
    sparkModel.fit(rddDataSet)

    // Save the model after training
    ModelSerializer.writeModel(sparkModel.getNetwork, new File("LLM_Spark_Model.zip"), true);

    // Show stats before ending spark context
    System.out.println("Total executors: " + sc.getExecutorMemoryStatus.size)
    sc.stop()

    logger.debug("Exiting SlidingWindow...")
  }
}
