import sbt._
import Versions._

object Dependencies {

  implicit class Exclude(module: ModuleID) {
    def log4jExclude: ModuleID =
      module excludeAll(ExclusionRule("log4j"))

    def driverExclusions: ModuleID =
      module.log4jExclude.exclude("com.google.guava", "guava")
        .excludeAll(ExclusionRule("org.slf4j"))
  }

  val kafkaStreams = "org.apache.kafka"            % "kafka-streams"                    % KafkaVersion
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging"                    % ScalaLoggingVersion
  val logback = "ch.qos.logback"                   % "logback-classic"                  % LogbackVersion
  val kafka = "org.apache.kafka"                  %% "kafka"                            % KafkaVersion
  val embeddedKafka = "net.manub"                 %% "scalatest-embedded-kafka"         % EmbeddedKafkaVersion
  val embeddedKafkaStreams = "net.manub"          %% "scalatest-embedded-kafka-streams" % EmbeddedKafkaVersion
}
