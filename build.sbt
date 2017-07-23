name := "job_runner"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
  "com.typesafe" % "config" % "1.3.1",
  "com.google.inject" % "guice" % "4.1.0",
  "com.h2database" % "h2" % "1.4.196" % "test",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scala-lang.modules" %% "scala-async" % "0.9.6",
  "mysql" % "mysql-connector-java" % "6.0.6"
)

unmanagedResourceDirectories in Compile := Seq(baseDirectory.value / "src/conf")
