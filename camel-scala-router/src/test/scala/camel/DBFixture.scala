package camel

import scala.slick.driver.PostgresDriver.simple._
import org.slf4j.LoggerFactory

import org.scalatest.{Inside, OptionValues, Matchers, FlatSpec}

class UnitSpec extends FlatSpec with Matchers with OptionValues with Inside
class DBUnitSpec extends  UnitSpec with DBFixture with TestDBProvider


/**
 * Created by boris on 12/7/13.
 */
trait DBFixture {
  provider:DBProvider =>

  val ddl = (DBModel.matches.ddl ++ DBModel.teams.ddl)

  val logger = LoggerFactory.getLogger(this.getClass)

  def withDatabase(testCode: Session => Any) {

    removeDb()
    createDb()
    try {
      testCode(session)
    }
    finally removeDb()
  }

  def session = db.createSession()

  def createDb()= {
    try{
      db.withSession{ implicit sess =>
        ddl.create
      }
    }
    catch{
      case e:Exception => logger.warn("Creating DB:",e.getMessage)
    }
  }
  def removeDb() {
    try{
      db.withSession{ implicit sess =>
        ddl.drop
      }
    }
    catch{
      case e:Exception => logger.warn("Dropping DB:",e.getMessage)
    }
  }

}

trait TestDBProvider extends DBProvider {
  def credentials = ("org.postgresql.Driver","jdbc:postgresql://localhost/hockey-test","hockey","hockey")
}

