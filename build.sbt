name := "MineScala"

version := "0.1"
scalaVersion := "2.13.1"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.8",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.26",
  "com.typesafe.akka" %% "akka-stream" % "2.5.26",
  "com.typesafe.akka" %% "akka-http" % "10.1.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.26" % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.23" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.10" % "test"
)

import scala.sys.process.Process

lazy val jsBuild = taskKey[Unit]("Run webpack when packaging the application")
lazy val jsLint = taskKey[Unit]("Run npm lint")
lazy val jsInstall = taskKey[Unit]("Run npm install")
lazy val jsUpdate = taskKey[Unit]("Run npm update")
lazy val jsAudit = taskKey[Unit]("Run npm audit")
lazy val jsAuditFix = taskKey[Unit]("Run npm audit fix")

def execCommand(command: Seq[String], cwd: Option[File] = None): Int = {
  val os = sys.props("os.name").toLowerCase
  val newCommand = os match {
    case x if x contains "windows" => Seq("cmd", "/C") ++ command
    case _                         => command
  }
  println(s"Running ${newCommand.mkString(" ")}")
  cwd match {
    case Some(file) => Process(newCommand, file).!
    case None       => Process(newCommand).!
  }
}

def runJsBuild(file: File): Int =
  execCommand(Seq("npm", "run", "build"), Some(file))
def runJsLint: Int = execCommand(Seq("npm", "run", "lint"))
def runJsInstall: Int = execCommand(Seq("npm", "install"))
def runJsUpdate: Int = execCommand(Seq("npm", "update"))
def runJsAudit: Int = execCommand(Seq("npm", "audit"))
def runJsAuditFix: Int = execCommand(Seq("npm", "audit", "fix"))

def npmError = throw new Exception("Something went wrong when running npm.")

jsBuild := {
  if (runJsBuild(baseDirectory.value) != 0) npmError
}

jsLint := {
  if (runJsLint != 0) npmError
}

jsInstall := {
  if (runJsInstall != 0) npmError
}

jsUpdate := {
  if (runJsUpdate != 0) npmError
}

jsAudit := {
  if (runJsAudit != 0) npmError
}

jsAuditFix := {
  if (runJsAuditFix != 0) npmError
}

enablePlugins(JavaAppPackaging)
