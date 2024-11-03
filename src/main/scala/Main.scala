package org.cs441homework2.com

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.Text

import java.io.PrintWriter
import scala.io.Source.fromInputStream

object Main extends LazyLogging {
  def deleteDirectory(dir: String): Unit = {
    logger.debug("Started deleteDirectory")

    val conf = new Configuration()
    val fs = FileSystem.get(conf)
    val path = new Path(dir)

    logger.debug("Checking directory")
    if (fs.exists(path)) {
      logger.debug("Deleting directory")
      fs.delete(path, true) // true to recursively delete
    }

    logger.debug("Exiting deleteDirectory...")
  }

  def removeKeyFromFile(fileDir: String) : Unit = {
    logger.debug("Entered removeKeyFromFile")

    val conf = new Configuration()

    val filePath = new Path(fileDir)

    // Get the FileSystem
    val fs = FileSystem.get(conf)

    // Open the file and get an InputStream
    val inputStream = fs.open(filePath)

    val content: String = fromInputStream(inputStream).mkString

    inputStream.close()

    // Store the content
    val fileContent = content

    val buffer = new Text("")

    val tempArray = fileContent.split("\\n")
    tempArray.foreach { sentence =>
      buffer.set(buffer.toString + "\n" + sentence.trim)
    }

    buffer.set(buffer.toString.substring(1, buffer.toString.length - 2))

    val writer = new PrintWriter(fileDir)

    try {
      writer.write(buffer.toString)
    } finally {
      writer.close()
    }

    logger.debug("Exiting removeKeyFromFile...")
  }

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val inputPath = config.getString("input.filePath")
    val outputPath = config.getString("output.outputPath")

    deleteDirectory(outputPath)
    Tokenization.tokenizationMR(inputPath, outputPath)

    // Modify part-00000 to exclude the keys and keep the values
    removeKeyFromFile(outputPath + "part-00000")

    // Use part-00000 and feed it into Word2VecModel
    // Refer to Embedding.scala for this function
    Embedding.runEmbedding(outputPath + "part-00000", outputPath + "answer")

    SlidingWindow.startOfProcess(outputPath + "answer", outputPath)
  }
}