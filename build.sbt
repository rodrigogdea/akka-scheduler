name := "simple-scheduler"

version := "1.0"

organization := "org.rgdea"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.2",
//  "org.scalactic" %% "scalactic" % "2.2.6",
  "joda-time" % "joda-time" % "2.9.4",

  "com.typesafe.akka" %% "akka-testkit" % "2.4.8" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "junit" % "junit" % "4.12" % "test"
)