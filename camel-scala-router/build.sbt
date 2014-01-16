name := "camel-scala-router"

version := "1.0"

scalaVersion := "2.10.2"


libraryDependencies += "org.apache.camel" % "camel-scala" % "2.12.2"

libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.5"

libraryDependencies += "log4j" % "log4j" % "1.2.17"

libraryDependencies += "com.typesafe.slick" %% "slick" % "2.0.0-M3"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.4"

libraryDependencies += "postgresql" % "postgresql" % "9.1-901.jdbc4"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" %  "test"

libraryDependencies +=  "org.apache.activemq" % "activemq-core" % "5.6.0"

libraryDependencies += "org.apache.camel" % "camel-jms" % "2.12.2"
