name := "job_runner"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
  "com.h2database" % "h2" % "1.4.196",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "org.scala-lang.modules" %% "scala-async" % "0.9.6",
  "mysql" % "mysql-connector-java" % "6.0.6"
)

routesGenerator := InjectedRoutesGenerator
