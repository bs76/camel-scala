package camel

import org.apache.camel.scala.dsl.builder.{RouteBuilderSupport, RouteBuilder}
import org.slf4j.LoggerFactory
import org.apache.camel.Exchange
import java.sql.Timestamp
import java.text.SimpleDateFormat
import scala.xml.Elem
import scala.util.{Try,Success,Failure}

/**
 * Created by boris on 12/7/13.
 */




class FileRouterBuilder extends RouteBuilder with RouteBuilderSupport{
  import FileRouterBuilder._


  "file:data/incoming?doneFileName=${file:name}.ready" ==> {
    log("log:got a new file")
    convertBodyTo(classOf[String])
    process{ e:Exchange =>
      val lines = e.in.asInstanceOf[String].split("\n").filterNot(_.trim().isEmpty).toList
      lines match {
        case mtch::timeStr::scoreStr::Nil => {
          parseMsg(mtch,timeStr,scoreStr) match {
            case Some((id,time,score:(Int,Int))) => {
              DBModel.updateMatchScore(id,score,time)
              val m = e.getIn()
              m.setHeader("matchid",id)

              val tstr = sdf.format(time)

              val body = <update match={id.toString} time={tstr}>
                          <home>{score._1.toString}</home>
                          <guest>{score._2.toString}</guest>
                        </update>

              e.in = body.toString()
            }
            case _ => {
              logger.error(s"Got wrong match message:\n $lines")
              //send to error queue

            }
          }
        }
        case _ => {
          logger.error(s"Got wrong match message:\n $lines")
          //send to error queue
        }
      }
    }
    choice{
       when( _.header("matchid") != null){
         to("jms:queue:updateToPlay")
       }
       otherwise {
         log(s"Error while receiving message")
       }
    }
  }


  //This is the route from play; save to DB send back to play
  "jms:queue:updateFromPlay" ==> {
    log("Got message ${in.body}")
    to("direct:updateScoreInDB")
    process{ e:Exchange =>
      println("inside")

    }
    choice{
      when(_.getIn.getHeader("success",classOf[Boolean]) == true){
        to("jms:queue:updateToPlay")
      }
      otherwise{
        log("Processing message failed: ${in.body}")
      }
    }
  }

  "direct:updateScoreInDB" ==> {
    process{ e:Exchange =>
      val body = e.getIn.getBody(classOf[Elem])
      FileRouterBuilder.parseMsgFromXML(body).map{ case (matchid,time,(home,guest)) =>
        DBModel.updateMatchScore(matchid,(home,guest),time)
        e.getIn.setHeader("success",true)
      }
    }
  }
}


object FileRouterBuilder{
  val logger = LoggerFactory.getLogger(classOf[FileRouterBuilder])

  val sdf = new SimpleDateFormat("HH:mm:ss")

  def parseMsgFromXML(body:Elem):Option[(Long,Timestamp,(Int,Int))] = {
    Try({

      val matchid  = (body \ "@match").text.toLong
      val timeStr = (body \ "@time").text
      val home = (body \ "home").text.toInt
      val guest = (body \ "guest").text.toInt

      val time = new Timestamp(sdf.parse(timeStr).getTime)

      (matchid,time,(home,guest))

    }).toOption
  }




  def parseMsg(matchIdStr:String,timeStr:String,scoreStr:String):Option[(Long,Timestamp,(Int,Int))] = {

    def parseId(matchId:String) = Try{ matchId.toLong }
    def parseScore(matchId:String) = Try{
      scoreStr.split(":").toList match {
        case home::guest::Nil => (home.toInt,guest.toInt)
        case _ => throw new IllegalArgumentException("Wrong format")
      }
    }
    def parseTime(timeString:String) = Try{
      new Timestamp(sdf.parse(timeStr).getTime)
    }

    val r = for{ id <- parseId(matchIdStr)
                 score <- parseScore(scoreStr)
                 time <- parseTime(timeStr)
    }
    yield (id,time,score)

    r.toOption
  }

}