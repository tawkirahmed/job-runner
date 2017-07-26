package services

import javax.inject.Inject

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
class EmailService @Inject()() {
  def sendMail(emails: Seq[String], message: String): Unit = {
    // TODO: Implement it
    println(s"Sending message to following emails: ${emails.mkString(System.lineSeparator)} ${System.lineSeparator} The email is $message")
  }
}
