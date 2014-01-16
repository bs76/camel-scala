package actors

import akka.camel.{Oneway, Producer}
import akka.actor.Actor

/**
 * Created by boris on 12/18/13.
 */
class ScoreUpdateActor extends Actor with Producer with Oneway{
  def endpointUri = "jms:queue:updateFromPlay"

}
