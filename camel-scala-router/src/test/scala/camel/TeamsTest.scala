package camel

/**
 * Created by boris on 12/7/13.
 */


class TeamsTest extends DBUnitSpec {
  import DBModel.teams
  import scala.slick.driver.PostgresDriver.simple._

  def countTeams(implicit session:Session) =  Query(teams.length).list().head


  "teams" should "contain new team after insert" in withDatabase{ implicit session =>

        teams.map(t => (t.name,t.home)) += ("Team1","Team1 Home")

        var cnt = countTeams
        println(s"count: ${cnt}")
        assert(cnt == 1)

        teams.map(t => (t.name,t.home)) += ("Team2","Team1 Home")
        cnt = countTeams
        println(s"count: ${cnt}")
        assert(cnt == 2)

  }
  "teams" must "be empty when adding and removing one" in withDatabase{ implicit session =>

    val key = (teams.map(t => (t.name,t.home)) returning teams.map(_.id)) += ("Team1","Team1 Home")
    println(s"key: $key")
    teams.where(_.id === key).delete
    val cnt = countTeams
    assert(cnt == 0)
  }

  "teams" must "must have unique name" in withDatabase{ implicit session =>
    intercept[ org.postgresql.util.PSQLException] {
      //this passes
      teams.map(t => (t.name,t.home)) += ("Team1","Team1 Home")
      //this fails
      teams.map(t => (t.name,t.home)) += ("Team1","Team1 Home")
    }

    //this passes, count is now 2
    teams.map(t => (t.name,t.home)) += ("Team2","Team1 Home")

    val cnt = countTeams
    assert(cnt == 2)

  }

  "teams" must "contain insert values" in withDatabase{ implicit session =>
    val key = (teams.map(t => (t.name,t.home)) returning teams.map(_.id)) += ("Team1","Team1 Home")

    val all = teams.where(_.id === key).list();

    assert(all.size == 1)

    val (id,name,town) = all.head

    assert(id == key && name == "Team1" && "Team1 Home" == town )
  }
}