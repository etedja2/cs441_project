package org.cs441homework2.com

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.EncodingType
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io._
import org.apache.hadoop.mapred.{FileInputFormat, FileOutputFormat, JobClient, JobConf, MapReduceBase, Mapper, OutputCollector, Reducer, Reporter, TextInputFormat, TextOutputFormat}

import java.io.IOException
import java.util
import scala.collection.convert.ImplicitConversions.`iterator asScala`

object Tokenization extends LazyLogging {

  class Map extends MapReduceBase with Mapper[LongWritable, Text, Text, Text] {

    // Declare required val (immutable) here
    private val word = new Text()

    // Jtokkit stuff
    private val registry = Encodings.newDefaultEncodingRegistry
    private val enc = registry.getEncoding(EncodingType.CL100K_BASE)

    @throws[IOException]
    override def map(key: LongWritable, value: Text, output: OutputCollector[Text, Text], reporter: Reporter): Unit = {
      logger.debug("Entering map()")

      // Split the documents by spaces, each word is called a token.
      // Each token will then be converted into int using Jtokkit enc.encode()

      val line: String = value.toString
      val buffer = new Text()

      // Enter the line
      line.split("\\n").foreach { sentence =>
        // Keep track of each sentence
        sentence.split(" ").foreach { token =>
          word.set(token)
          // Tokenize it
          val encoded = enc.encode(token)
          val encoded_val = new Text(encoded.get(0).toString)

          buffer.set(buffer.toString + " " + encoded_val)
        }

        output.collect(new Text(key.toString), buffer)
      }

      logger.debug("Exiting map()...")
    }
  }

  class Reduce extends MapReduceBase with Reducer[Text, Text, Text, Text] {
    override def reduce(key: Text, values: util.Iterator[Text], output: OutputCollector[Text, Text], reporter: Reporter): Unit = {
      logger.debug("Entering class reduce()")
      // This function will convert the contents produced by map() into 1 single string.
      // This string will represent a sentence (in simple terms)

      // The part of the code that turns the stuff produced by map() into a single string
      val reducedOutput = values.reduce((sentence1, sentence2) =>
        new Text(sentence1.toString + "\n" + sentence2.toString)
      )

      logger.debug("Exiting class reduce()...")
      output.collect(new Text(""), reducedOutput)
    }
  }

  def tokenizationMR(inputPath: String, outputPath: String): Unit = {
    logger.debug("Entered tokenizationMR")

    val conf: JobConf = new JobConf(this.getClass)
    conf.setJobName("Tokenization")
    conf.set("fs.defaultFS", "local")
    conf.set("mapreduce.job.maps", "1")
    conf.set("mapreduce.job.reduces", "1")

    // Mapper and Reducer class
    conf.setMapperClass(classOf[Map])
    conf.setReducerClass(classOf[Reduce])

    // Output key and value types
    conf.setMapOutputKeyClass(classOf[Text])
    conf.setMapOutputValueClass(classOf[Text])

    conf.setOutputKeyClass(classOf[Text])
    conf.setOutputValueClass(classOf[Text])

    // Input and output paths
    FileInputFormat.setInputPaths(conf, new Path(inputPath))
    FileOutputFormat.setOutputPath(conf, new Path(outputPath))

    // Additional Stuff that IDK what to classify as
    conf.setCombinerClass(classOf[Reduce])
    conf.setInputFormat(classOf[TextInputFormat])
    conf.setOutputFormat(classOf[TextOutputFormat[Text, Text]])

    JobClient.runJob(conf)

    logger.debug("Exiting tokenizationMR...")
  }
}
