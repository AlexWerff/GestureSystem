name := "ModelBackend"

version := "1.0"

scalaVersion := "2.12.2"


libraryDependencies ++= {
   val akkaV       = "2.5.3"
   val akkaHttpV   = "10.0.9"
   val scalaTestV  = "3.0.1"
   Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaV,
      "com.typesafe.akka" %% "akka-stream" % akkaV,
      "com.typesafe.akka" %% "akka-http" % akkaHttpV,
      "org.scalatest"     %% "scalatest" % scalaTestV % "test",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
      "io.circe" %% "circe-core" % "0.8.0",
      "io.circe" %% "circe-generic" % "0.8.0",
      "io.circe" %% "circe-parser" % "0.8.0"
   )
}