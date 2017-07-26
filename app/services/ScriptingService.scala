package services

import javax.inject.Inject

import scala.sys.process.{ProcessLogger, _}

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
class ScriptingService @Inject()() {
  private val charSet = "UTF-8"

  def run(cmd: String): Long = {

    val commands = cmd.split(System.lineSeparator)

    commands.zipWithIndex.foldLeft(0l)((size, command) => {
      val cmdStatus = runCommand(command._1)

      if (cmdStatus.status != 0) {
        val message = getErrorMessage(command, cmdStatus.stderr)
        throw new Exception(message)
      }

      // TODO: The output might be very long, better redirect to file and get the size from there
      size + (cmdStatus.stdout.toString.getBytes(charSet)).length
    })
  }

  private def getErrorMessage(command: (String, Int), stderr: StringBuilder): String = {
    s"The command at line(${command._2 + 1}): ${command._1} failed with the following exception: ${System.lineSeparator}\t ${stderr.toString()}"
  }

  private def runCommand(command: String): CommandStatus = {
    val os = sys.props("os.name").toLowerCase

    val osTransformedCommand: Seq[String] = (os match {
      case x if x contains "windows" => Seq("cmd", "/C")
      case _ => Nil
    }) :+ command


    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val status = osTransformedCommand ! ProcessLogger(stdout append _, stderr append _)

    println(stdout)
    CommandStatus(status, stdout, stderr)
  }

  private case class CommandStatus(status: Int, stdout: StringBuilder, stderr: StringBuilder)

}
