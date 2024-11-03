ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.20"

lazy val root = (project in file("."))
  .settings(
    name := "Homework2",
    idePackagePrefix := Some("org.cs441homework2.com")
  )

val sparkVersion = "3.5.3"
val dl4jVersion = "1.0.0-M2.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion, // Spark Dependencies
  "org.apache.spark" %% "spark-sql" % sparkVersion % "compile",
  "org.apache.spark" %% "spark-mllib" % sparkVersion,

  "org.scalatest" %% "scalatest" % "3.2.16" % "test", // ScalaTest Dependencies
  "org.scalatestplus" %% "mockito-4-11" % "3.2.16.0" % "test", // ScalaTestPlus Mockito integration
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0", // Scala collection compatibility

  "org.apache.hadoop" % "hadoop-common" % "3.4.0", // Hadoop core libraries
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.4.0", // Hadoop MapReduce client
  "org.apache.hadoop" % "hadoop-client" % "3.4.0", // Hadoop client libraries (optional)

  "com.knuddels" % "jtokkit" % "1.1.0", // Jtokkit

  "org.deeplearning4j" %% "dl4j-spark" % dl4jVersion,
  "org.deeplearning4j" % "deeplearning4j-core" % dl4jVersion, // DeepLearning4j Dependencies
  "org.deeplearning4j" % "deeplearning4j-nlp" % dl4jVersion,
  "org.nd4j" % "nd4j-native-platform" % dl4jVersion,

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.5.6" // Logback for logging backend
)
