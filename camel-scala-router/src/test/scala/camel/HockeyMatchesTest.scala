package camel

import org.scalatest.FunSuite
import java.sql.Timestamp

/**
 * Created by boris on 12/7/13.
 */
class HockeyMatchesTest extends DBUnitSpec {
  import DBModel.matches
  import DBModel.teams
  import scala.slick.driver.PostgresDriver.simple._

  def countMatches(implicit session:Session) =  Query(matches.length).list().head

  def newTeam(name:String,town:String)(implicit session:Session):Long =
    (teams.map(t => (t.name,t.home)) returning teams.map(_.id)) += (name,town)

  def tstamp(ts:Long=System.currentTimeMillis()) = new Timestamp(ts)

  "matches" must "contain a new match after inserting" in withDatabase{ implicit session =>
    val k1 = newTeam("team1","town1")
    val k2 = newTeam("team2","town1")

    val start = tstamp()
    val lastUpd = tstamp()

    matches.map(m => (m.homeTeamId,m.guestTeamId,m.lastUpdate,m.started)) += (k1,k2,lastUpd,start)

    assert(countMatches == 1)

    val cnt1 = matches.where(_.guestTeamId === k2 ).list().size

    assert(cnt1 == 1)
  }

}
