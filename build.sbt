name := "MineScala"

version := "0.1"
scalaVersion := "2.13.1"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.8",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.26",
  "com.typesafe.akka" %% "akka-stream" % "2.5.26",
  "com.typesafe.akka" %% "akka-http" % "10.1.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.26" % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.23" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.10" % "test")

import scala.sys.process.Process

lazy val jsBuild = taskKey[Unit]("Run webpack when packaging the application")

def execCommand(command: Seq[String], cwd: Option[File] = None): Int = {
  val os = sys.props("os.name").toLowerCase
  val newCommand = os match {
    case x if x contains "windows" => Seq("cmd", "/C") ++ command
    case _ => command
  }
  println(s"Running ${newCommand.mkString(" ")}")
  cwd match {
    case Some(file) => Process(newCommand, file).!
    case None => Process(newCommand).!
  }
}

def runJsBuild(file: File): Int = {
  execCommand(Seq("npm", "run", "build"), Some(file))
}

jsBuild := {
  if (runJsBuild(baseDirectory.value) != 0) throw new Exception("Something went wrong when running npm.")
}