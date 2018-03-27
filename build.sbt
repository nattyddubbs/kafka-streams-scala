import Dependencies._

name := "kafka-streams-scala"

organization := "com.talentreef"

version := "0.2.0-SNAPSHOT"

isSnapshot := true

scalaVersion := Versions.Scala_2_12_Version

crossScalaVersions := Versions.CrossScalaVersions

scalacOptions := Seq("-Xexperimental", "-unchecked", "-Ywarn-unused-import")

parallelExecution in Test := false

libraryDependencies ++= Seq(
  kafkaStreams excludeAll(ExclusionRule("org.slf4j", "slf4j-log4j12"), ExclusionRule("org.apache.zookeeper", "zookeeper")),
  scalaLogging % "test",
  logback % "test",
  kafka % "test" excludeAll(ExclusionRule("org.slf4j", "slf4j-log4j12"), ExclusionRule("org.apache.zookeeper", "zookeeper")),
  embeddedKafka % "test",
  embeddedKafkaStreams % "test"
)

licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

organizationName := "talentreef"

organizationHomepage := Some(url("http://talentreef.com/"))

scmInfo := Some(ScmInfo(url("https://github.com/nattyddubbs/kafka-streams-scala"), "git@github.com:nattyddubbs/kafka-streams-scala.git"))

homepage := scmInfo.value map (_.browseUrl)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(
  "Nexus Repository Manager",
  "oss.sonatype.org",
  userName = System.getenv("OSS_USER"),
  passwd = System.getenv("OSS_PASSWORD")
)

publishArtifact in Test := false

publishMavenStyle := true
