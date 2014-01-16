package actors

import akka.actor.Actor
import play.api.libs.iteratee.{Enumerator, Concurrent}
import play.api.libs.json.JsValue

import play.api.libs.json.Json
import Json._

/**
 * Created by boris on 12/18/13.
 */
class WSActor extends Actor{

  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

  def receive = {
    case Register() => {
      println("Registered new client")
      sender ! Registered(chatEnumerator)
    }
    case ScoreUpdate(matchId,home,guest) => {
      val msg = mkBroadcastMsg(matchId,home,guest)
      println("Pushing out message to clients:"+msg.toString())
      chatChannel push msg
    }

    case x:Any => {
      println("Unknown message: "+x)
    }
  }

  def mkBroadcastMsg(matchId:Long,home:Int,guest:Int) = {
    Json.obj("update" -> Json.obj(
      "id" -> matchId,
      "home" -> home,
      "guest" -> guest
    ))
  }


 }

case class Register()
case class Registered(e:Enumerator[JsValue])
case class ScoreUpdate(matchid:Long,home:Int,guest:Int)
