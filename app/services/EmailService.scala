package services

import javax.inject.Inject

import com.typesafe.config.Config
import play.api.libs.mailer._

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
class EmailService @Inject()(cfg: Config, mailerClient: MailerClient) {

  def sendEmail(emails: Seq[String], message: String) = {
    val email = Email(
      "Job Notification",
      cfg.getString("mail.from"),
      emails,
      bodyText = Some("A text message"),
      bodyHtml = Some(s"""<html><body><p>$message</p></body></html>""")
    )

    mailerClient.send(email)
  }
}
