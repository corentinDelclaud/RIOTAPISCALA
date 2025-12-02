name := "mcp-riot-scala"
version := "0.1.0"
scalaVersion := "3.6.3"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.1.14",
  "dev.zio" %% "zio-http" % "3.0.1",
  "dev.zio" %% "zio-json" % "0.7.3",
  "dev.zio" %% "zio-config" % "4.0.2",
  "dev.zio" %% "zio-config-typesafe" % "4.0.2",
  "io.getquill" %% "quill-zio" % "4.8.5",
  "com.softwaremill.sttp.client3" %% "core" % "3.10.2",
  "com.softwaremill.sttp.client3" %% "zio" % "3.10.2",
  "com.softwaremill.sttp.client3" %% "zio-json" % "3.10.2",
  "ch.qos.logback" % "logback-classic" % "1.5.12"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked"
)
