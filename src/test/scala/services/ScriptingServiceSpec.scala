package services

import org.scalatest.FlatSpec

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
class ScriptingServiceSpec extends FlatSpec {

  val scriptService = new ScriptingService

  "run" should "execute single command string" in {
    val cmd = "cd"
    val size = scriptService.run(cmd)
    println("Output size: " + size)
  }

  "run" should "execute multiple command string" in {
    val cmd = "cd" + System.lineSeparator + "cd"
    val size = scriptService.run(cmd)
    println("Output size: " + size)
  }

  "run" should "throw exceptions with invalid command string" in {
    val cmd = "cdl"
    assertThrows[java.lang.Exception] {
      scriptService.run(cmd)
    }
  }

  "run" should "throw exceptions with invalid command present alogn with valid command string" in {
    val cmd = "cdl" + System.lineSeparator + "cd"
    assertThrows[java.lang.Exception] {
      scriptService.run(cmd)
    }
  }

  "run" should "throw exceptions plus details with invalid command present along with valid command string" in {
    val cmd = "cd" + System.lineSeparator + "cdl" + System.lineSeparator + "cd"

    val caught =
      intercept[java.lang.Exception] {
        scriptService.run(cmd)
      }

    assert(caught.getMessage.contains("at line(2)"))
  }
}
