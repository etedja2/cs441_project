package org.cs441homework2.com

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

import java.io.File
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter

object GenerateSentence {
  // Jtokkit stuff
  val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry
  val enc: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

  private def createEmbeddings(context: Array[String], word2VecModel: Word2Vec): INDArray = {
    val targetSize = 4

//    println(word2VecModel.hasWord("791"))

    val embeddings = context.map { word =>
      val token = enc.encode(word).get(0).toString

//      println("This is current word: " + token)
      if (word2VecModel.hasWord(token)) {
//        println("Found")
        word2VecModel.getWordVector(token).take(targetSize)
      } else {
//        println("Not found")
        Array.fill(targetSize)(0.0) // Return a zero vector if the word is not in the vocabulary
      }
    }

    // Convert the embeddings to an ND4J INDArray
    Nd4j.create(embeddings)
  }

  private def wordIndex2Word (nextWordIndex: Int, vocabMap: Map[Int, String]): String = {
    vocabMap.getOrElse(nextWordIndex, "<UNKNOWN>")
  }

  private def generateNextWord(context: Array[String], modelPath: String, embedPath: String): String = {
    // Extract the vocabulary from Word2Vec
    val word2VecModel: Word2Vec = WordVectorSerializer.readWord2VecModel(new java.io.File(embedPath))

    // (_.swap) is used because vocabMap is in the format of [String, Int], while [Int, String] is required.
    val vocabMap = word2VecModel.getVocab.words.asScala.zipWithIndex.map(_.swap).toMap

    // Load the DL4J model from the file
    val model: MultiLayerNetwork = ModelSerializer.restoreMultiLayerNetwork(modelPath)
    val embeddings = model.output(createEmbeddings(context, word2VecModel))

//    println(s"Embeddings shape: ${embeddings.shape.mkString(", ")}")
//    println(s"Model output: ${embeddings}")
//    vocabMap.foreach { case (index, word) =>
//      println(s"Index: $index, Word: $word")
//    }
//    println(s"Model summary ${model.summary}")

    // argMax() processes 2 layers, getInt for the first index
    val nextWordIndex = Nd4j.argMax(embeddings, 1).getInt(0)

//    println(nextWordIndex)
//    println(embeddings)

    // Turn the selected word's index into a string based on vocabulary list
    wordIndex2Word(nextWordIndex, vocabMap)
  }

  private def generateSentence(query: String, modelPath: String, sentenceLen: Int, embedPath: String): String = {
    val context = query.split(" ")
    val tokenList = new IntArrayList()

    (0 until sentenceLen).toList.foreach(_ =>
      tokenList.add(generateNextWord(context, modelPath, embedPath).toInt)
    )

    enc.decode(tokenList)
  }

  def startingPoint (outputPath: String, query: String): String = {
//    val query = "what is time horizon" // This will be the prompt to my model.
    val len = 4 // Length of response from my model
    val modelPath = outputPath + "trained_model.zip"
    val embedPath = outputPath + "answer"

    val generatedSentence = generateSentence(query, modelPath, len, embedPath)

//    println(generatedSentence)
    generatedSentence
  }
}
