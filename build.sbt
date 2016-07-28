name := "akka-scheduler"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.8",
  "org.scalactic" %% "scalactic" % "2.2.6",

  "com.typesafe.akka" %% "akka-testkit" % "2.4.8" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)