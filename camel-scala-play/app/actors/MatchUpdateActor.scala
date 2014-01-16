package actors

import play.api
import akka.actor.{ActorRef, Actor}
import akka.camel.{Oneway, CamelMessage, Consumer}
import xml._
import java.sql.Timestamp
import java.text.SimpleDateFormat
import scala.util.Try

/**
 * Created by boris on 12/8/13.
 */
class MatchUpdateActor(wsNotifyActor:ActorRef) extends Consumer {

  def endpointUri = "jms:queue:updateToPlay"

  override def autoAck = true

  def receive = {
    case msg:CamelMessage => {

      //These do not work:
      // msg.bodyAs[Elem]
      // msg.bodyAs[Document]

      val bodyStr = msg.bodyAs[String]
      val body = XML.loadString(bodyStr)

      val matchIdHdr = msg.headerAs[Long]("matchid")

      println(s"Got body: $body")
      println(s"match id: $matchIdHdr")

      parseMsg(body).map{ parsed =>
        println(s"Parsed: $parsed")

        wsNotifyActor ! ScoreUpdate(parsed._1,parsed._3,parsed._4)
      }
    }

    case x:Any => {
      println("Unknown message:"+ x)
    }
  }

  def parseMsg(body:Elem):Option[(Long,Timestamp,Int,Int)] =
    Try({
      val matchid  = (body \ "@match").text.toLong
      val timeStr = (body \ "@time").text
      val home = (body \ "home").text.toInt
      val guest = (body \ "guest").text.toInt

      val time = new Timestamp(Helper.matchTimeFormatter.parse(timeStr).getTime)

      (matchid,time,home,guest)
    }).toOption


}
