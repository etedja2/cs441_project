package org.cs441homework2.com

import com.typesafe.scalalogging.LazyLogging
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.sentenceiterator.{BasicLineIterator, SentenceIterator}

import java.io.File

object Embedding extends LazyLogging {
  def runEmbedding(inputPath: String, outputPath: String): Unit = {
    logger.debug("Starting Embedding.scala")

    val iter:SentenceIterator = new BasicLineIterator( new File (inputPath) )

    logger.debug("Building Word2Vec model...");
    val vec:Word2Vec = new Word2Vec.Builder()
      .minWordFrequency(1)
      .layerSize(100)
      .seed(42)
      .windowSize(5)
      .iterate(iter)
      .build();

    logger.debug("Fitting Word2Vec model...");

    // Fit can be seen as the act of giving data to the model
    vec.fit();

    WordVectorSerializer.writeWordVectors(vec, outputPath)

    logger.debug("Finished Embedding.scala")
  }
}
