package camel

import java.sql.Timestamp
import scala.slick.driver.PostgresDriver.simple._


case class Team(id:Option[Long],name:String,homeTown:String)
case class HockeyMatch(id:Option[Long],homeTeam:Team,guestTeam:Team,scoreHome:Int,scoreGuest:Int,lastUpdate:Timestamp)

trait DBProvider{
  def credentials:(String,String,String,String)

  private lazy val _db:Database = {
    val (driver,url,user,pass) = credentials
    Database.forURL(driver=driver,url=url,user=user,password = pass)
  }

  def db:Database = _db
}

trait ProdDBProvider extends  DBProvider{
  def credentials = ("org.postgresql.Driver","jdbc:postgresql://localhost/hockey","hockey","hockey")
}

object DBModel extends  ProdDBProvider {
  import scala.slick.driver.PostgresDriver.simple._

  class Teams(tag:Tag) extends Table[(Long,String,String)](tag,"team"){
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def name = column[String]("NAME",O.DBType("varchar(50)"))
    def home= column[String]("HOMETOWN",O.DBType("varchar(256)"))

    def * = (id, name , home)

    def uniqueName = index("IDX_NAME", name, unique = true)
  }

  class HockeyMatches(tag:Tag) extends Table[(Long,Long,Long,Int,Int,Timestamp,Timestamp,Boolean)](tag,"match"){
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def homeTeamId = column[Long]("HOME_TEAM_ID",O.NotNull)
    def guestTeamId = column[Long]("GUEST_TEAM_ID",O.NotNull)
    def scoreHome = column[Int]("SCORE_HOME",O.Default(0), O.NotNull)
    def scoreGuest = column[Int]("SCORE_GUEST",O.Default(0), O.NotNull)
    def lastUpdate = column[Timestamp]("LASTUPDATE",O.NotNull)
    def started = column[Timestamp]("STARTED")
    def inProgress = column[Boolean]("INPROGRESS",O.Default(false));

    def * = (id,homeTeamId,guestTeamId,scoreHome, scoreGuest,lastUpdate,started,inProgress)  //<> (HockeyMatch.tupled,HockeyMatch.unapply)

    def homeTeam = foreignKey("fk_home_team",homeTeamId,teams)(_.id)
    def guestTeam = foreignKey("fk_guest_team",guestTeamId,teams)(_.id)

  }

//  object HockeyMatches extends Table[Match]

  val teams = TableQuery[Teams]
  val matches = TableQuery[HockeyMatches]

  def updateMatchScore(matchId:Long,score:(Int,Int),lastUpdate:Timestamp) = {
    db.withSession{ implicit session =>
      val qForUpd = for{ m <- matches if m.id === matchId}
      yield (m.scoreHome,m.scoreGuest,m.lastUpdate)

      qForUpd.update(score._1,score._2,lastUpdate)

    }


  }


}