name := "camel-scala-play"

version := "1.0-SNAPSHOT"


libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

libraryDependencies +=   "com.typesafe.akka" % "akka-camel_2.10" % "2.2.0"

libraryDependencies +=  "org.apache.activemq" % "activemq-core" % "5.6.0"

libraryDependencies += "org.apache.camel" % "camel-jms" % "2.12.2"

libraryDependencies += "postgresql" % "postgresql" % "9.1-901.jdbc4"


play.Project.playScalaSettings
