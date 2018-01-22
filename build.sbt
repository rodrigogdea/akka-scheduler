name := "simple-scheduler"

version := "1.0.2"

organization := "org.rgdea"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
//  "org.scalax" %% "scala-boolean-extension" % "1.0",
  "joda-time" % "joda-time" % "2.9.9",

  "com.typesafe.akka" %% "akka-testkit" % "2.5.9" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "junit" % "junit" % "4.12" % "test"
)