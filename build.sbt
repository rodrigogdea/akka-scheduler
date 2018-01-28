name := "simple-scheduler"

version := "1.0.2"

organization := "org.rgdea"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "com.typesafe.akka" %% "akka-stream" % "2.5.9",
  "joda-time" % "joda-time" % "2.9.9",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.9" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.9" % Test,
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "junit" % "junit" % "4.12" % Test
)
