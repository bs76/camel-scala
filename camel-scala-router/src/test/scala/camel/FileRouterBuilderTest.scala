package camel

import org.scalatest.FunSuite

/**
 * Created by boris on 12/8/13.
 */
class FileRouterBuilderTest extends UnitSpec{



  "parsing a messeage" must "pass when format is correct" in {
    val id = "1"
    val time = "0:22:15"
    val score = "2:0"

    val (i,t,s) = parse(id,time,score).get

    assert(i == 1L)
    assert(t.getTime == 55119615000L)
    assert(s == (2,0))

  }

  "parsing a bad messeage" must "fail" in {
    val id = "1"
    val time = "0:22:15"
    val score = "2:0"

    var r = parse("A",time,score)

    assert( !r.isDefined)

    r = parse(id,"xxx",score)
    assert( !r.isDefined)

    r = parse(id,time,"20")
    assert( !r.isDefined)

    r = parse(null,time,"20")
    assert( !r.isDefined)

    r = parse(null,null,null)
    assert( !r.isDefined)

    r = parse(id,"22:22",score)
    assert( !r.isDefined)
  }

  def parse(id:String,time:String,score:String) = FileRouterBuilder.parseMsg(id,time,score)

}
