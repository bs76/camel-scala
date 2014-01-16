camel-scala
===========

Demo for Slovak Scala User Group showing:

* camel routes written in scala
* integration of camel and akka

What you need to run it
=========================

* PostgreSQL installation (see scala-camel-router/src/main/scala/camel/slick.scala)
* ActiveMQ 
  * download 
  * start
  * manually create queues: updateToPlay, updateFromPlay

What it does and how
====================

camel-scala-router is a standalone router that waits for incoming files in data/incoming. Once a file
is dropped into data/incoming (e.g. data.txt) *AND* data.txt.ready files exists in the same location
router processes the incoming file.

The expected format of the file is:
* 3 lines
* 1st line: match id (see slick.scala)
* 2nd line: time in format hh:mm:dd
* 3rd line: score

Once the file is processed it is saved into the DB, then a message is sent via JMS (ActiveMQ) to play
application's actor that use play camel plugin

camel-scala-play is a play application that:
* can receive messages from scala router via JMS.
* it displays hockey match scores 
* it can update the score of a match 
* when score update is received scores are updates using WebScokets so users can see changes live

