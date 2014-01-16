package camel

import org.slf4j.LoggerFactory
import java.sql.Timestamp

/**
 * Created by boris on 12/7/13.
 */
object CreateDB extends  App with ProdDBProvider {
  import scala.slick.driver.PostgresDriver.simple._
  import DBModel.teams
  import DBModel.matches


  val logger = LoggerFactory.getLogger(CreateDB.getClass)

  db withSession { implicit session =>
      val ddl = DBModel.teams.ddl ++ DBModel.matches.ddl


      //ddl.drop
      ddl.create

      val theTeams = Seq(
        ("CSKA","Moskva"),
        ("Slovan ","Bratislava"),
        ("Lev","Praha"),
        ("Atlant","Moskva"),
        ("SKA","Petrohrad"),
        ("Spartak","Moskva")
      )

      val insertTeam = (teams.map(t => (t.name,t.home))  returning teams.map(_.id))

      val ids = theTeams.map{ t => insertTeam += t }

      val theMatches = ids.zip(ids.reverse)

      val insertMatch = matches.map(m => (m.homeTeamId,m.guestTeamId,m.lastUpdate,m.started,m.inProgress))
      val now = new Timestamp(0)

      theMatches.map{ case (h,g) => insertMatch += (h,g,now,now,false) }

  }

}
