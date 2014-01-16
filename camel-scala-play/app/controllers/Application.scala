package controllers

import play.api._
import play.api.mvc._
import play.api.db.DB
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.json._
import concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Akka
import actors.{Helper, Registered, Register}
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.data._
import play.api.data.Forms._
import akka.camel.CamelMessage
import java.util.Date


object Application extends Controller {

  lazy val wsNotifyActor = Akka.system.actorSelection("/user/wsActor")
  lazy val scoreUpdateActor = Akka.system.actorSelection("/user/scoreUpdateActor")

  val scoreUpdateForm = Form(
    tuple(
      "home" -> number(min=0),
      "guest" -> number(min=0)
    )
  )

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def listMatches = Action {
    val res = findAllMatches

    Ok(views.html.list(res))
  }

  def listMatchesForUpdate = Action {
    val res = findAllMatches

    Ok(views.html.listForUpdate(res))
  }

  def updateWs = WebSocket.using[JsValue] { request =>

    implicit val timeout = Timeout(5 seconds)
    val future  =  wsNotifyActor ? Register()

    val out = Await.result(future, timeout.duration).asInstanceOf[Registered]

    val in = Iteratee.foreach[JsValue] {
      msg => println(msg)
    }
    (in, out.e)
  }

  def updateForm(matchId:Long) = Action {
    val ml = findMatchById(matchId).toList

    ml.headOption.map{ m =>
      val frm = scoreUpdateForm.bind(Map("home" -> m._2.toString,"guest" -> m._3.toString))
      Ok(views.html.updateScore(m,frm))
    }.getOrElse(BadRequest("No such match"))

  }

  def update(id:Long) = Action{ implicit request =>

    scoreUpdateForm.bindFromRequest.fold(
      formWithErrors => {
        findMatchById(id).toList.headOption.map{ m =>
            BadRequest(views.html.updateScore(m,formWithErrors))
        }.getOrElse(BadRequest("Match does not exist"))
      },
      value => {
        val (home,guest) = value

        println(s"home: $home guest: $guest")

        val timeStr = Helper.matchTimeFormatter.format(new Date())

        val xmlMsg = <update match={id.toString} time={timeStr}>
          <home>{home.toString}</home>
          <guest>{guest.toString}</guest>
        </update>

        val msg = CamelMessage(body = xmlMsg,headers = Map("matchid" -> id))

        scoreUpdateActor ! msg
        Redirect(routes.Application.listMatchesForUpdate)
      }
    )
  }

  def findMatchById(id:Long) = {
    DB.withConnection{ implicit conn =>
      SQL(findMatchSql).on("id"->id).as(
        long("ID") ~
          int("SCORE_HOME") ~
          int("SCORE_GUEST") ~
          str("THNAME") ~
          str("THHOMETOWN") ~
          str("TGNAME") ~
          str("TGHOMETOWN")
          map(flatten) *)
    }
  }

  def findAllMatches = {
    DB.withConnection{ implicit conn =>
      SQL(matchSql).as(
          long("ID") ~
          int("SCORE_HOME") ~
          int("SCORE_GUEST") ~
          str("THNAME") ~
          str("THHOMETOWN") ~
          str("TGNAME") ~
          str("TGHOMETOWN")
          map(flatten) *)
    }
  }

  implicit val timeout = Timeout(5 seconds)

  val matchSql = """
                   |select
                   |  m."ID",
                   |  m."SCORE_HOME",
                   |  m."SCORE_GUEST",
                   |  th."NAME" as "THNAME",
                   |  th."HOMETOWN" as "THHOMETOWN",
                   |  tg."NAME" as "TGNAME",
                   |  tg."HOMETOWN" as "TGHOMETOWN"
                   |from
                   |  team th,
                   |  team tg,
                   |  match m
                   |where
                   |  m."HOME_TEAM_ID"=th."ID" and
                   |  m."GUEST_TEAM_ID"=tg."ID"
                   |order by "ID"
                   |  """.stripMargin('|');


    val findMatchSql = """
                     |select
                     |  m."ID",
                     |  m."SCORE_HOME",
                     |  m."SCORE_GUEST",
                     |  th."NAME" as "THNAME",
                     |  th."HOMETOWN" as "THHOMETOWN",
                     |  tg."NAME" as "TGNAME",
                     |  tg."HOMETOWN" as "TGHOMETOWN"
                     |from
                     |  team th,
                     |  team tg,
                     |  match m
                     |where
                     |  m."HOME_TEAM_ID"=th."ID" and
                     |  m."GUEST_TEAM_ID"=tg."ID" and
                     |  m."ID"={id}
                     |  """.stripMargin('|');
}