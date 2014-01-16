/**
 * Created by boris on 12/8/13.
 */

import actors.{ScoreUpdateActor, WSActor, MatchUpdateActor}
import akka.actor.Props
import akka.camel.CamelExtension
import javax.jms.ConnectionFactory
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.camel.component.jms.JmsComponent
import play.api._
import play.libs.Akka

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")

    val system = play.libs.Akka.system()
    val camel = CamelExtension(system)
    val camelContext = camel.context

    val connectionFactory: ConnectionFactory = new ActiveMQConnectionFactory("tcp://0.0.0.0:61616")
    camelContext.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory))

    val wsActor = Akka.system.actorOf(Props[WSActor], name = "wsActor")

    val camelActor = Akka.system.actorOf(Props(new MatchUpdateActor(wsActor)), name = "matchUpdateActor")

    val scoreUpdateActor = Akka.system.actorOf(Props[ScoreUpdateActor], name = "scoreUpdateActor")

    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}