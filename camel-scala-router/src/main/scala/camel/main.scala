package camel

import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import org.slf4j.LoggerFactory
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.ConnectionFactory
import org.apache.camel.component.jms.JmsComponent
import org.apache.camel.impl.DefaultCamelContext


/**
 * Created by boris on 12/7/13.
 */
object Main extends App with RouteBuilderSupport{
  val logger = LoggerFactory.getLogger(getClass)

  logger.info("Starting")


  val ctx = new DefaultCamelContext();
  ctx.addRoutes(new FileRouterBuilder)
  val connectionFactory: ConnectionFactory = new ActiveMQConnectionFactory("tcp://0.0.0.0:61616")
  ctx.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory))

  ctx.start();



  while(true){
    Thread.sleep(60 * 1000)
    logger.info("Tick")
  }
}
//  val main =   new org.apache.camel.main.Main();
//  main.addRouteBuilder(new FileRouterBuilder)
// enable hangup support so you can press ctrl + c to terminate the JVM
//  main.enableHangupSupport();
//  main.start();
//  val context = main.getCamelContexts().get(0)
//
//  val connectionFactory: ConnectionFactory = new ActiveMQConnectionFactory("vm://localhost")
//  context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory))
//
//  main.run()