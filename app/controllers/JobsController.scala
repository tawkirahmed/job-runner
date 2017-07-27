package controllers

import javax.inject._

import akka.actor.ActorSystem
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import repositories.JobsRepository
import repositories.dtos.dtos._
import services.JobsService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JobsController @Inject() (cc: ControllerComponents,
  messagesApi: MessagesApi,
  actorSystem: ActorSystem,
  jobService: JobsService,
  jobRepo: JobsRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  import JobsController._

  def index = Action.async {
    jobRepo.findAll().map(job => Ok(views.html.jobs(job)))
  }

  def add() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.jobAdd(jobForm))
  }/*

  def saveContact = Action { implicit request =>
    contactForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.contact.form(formWithErrors))
      },
      contact => {
        val contactId = Contact.save(contact)
        Redirect(routes.Application.showContact(contactId)).flashing("success" -> "Contact saved!")
      }
    )
  }*/

  def save = Action.async(parse.formUrlEncoded(maxLength = 10000000)) { implicit req =>

      jobForm.bindFromRequest.fold(
        formWithErrors => {
          println("Errors: " + formWithErrors)
          Future(BadRequest(views.html.jobAdd(formWithErrors)))
        },
        userInput => {
          println("Input job: " + userInput)
          Future(userInput.job).map(x => Ok(views.html.jobDetails(x)))
        }

      )
    }

  def find(jobId: Int) = Action.async {
    jobRepo.find(jobId).map(x => Ok(views.html.jobDetails(x)))
  }

  def update(jobId: Int) = Action.async {
    jobRepo.find(jobId).map(job => Ok(views.html.jobUpdate(job)))
  }

  def delete(jobId: Int) = Action.async {
    jobRepo.delete(jobId).map(x => {
      Ok("Job is deleted.")
    })
  }

  def run(jobId: Int) = Action {
    // TODO: To be implemented
    Ok("Job has been started")
  }
}

object JobsController {

  case class JobForm(job: Job, executables: List[Executable], dependencies: List[Int], watchers: List[JobWatcher])

  val jobForm: Form[JobForm] = Form(
    mapping(
      "job" -> mapping(
        "id" -> optional(number),
        "name" -> text,
        "status" -> default(number, 1),
        "lastRunTime" -> optional(sqlDate),
        "runTime" -> optional(longNumber),
        "minimumDataOutputSize" -> optional(longNumber),
        "maximumDataOutputSize" -> optional(longNumber),
        "expectedDuration" -> optional(longNumber),
        "lastExecutionId" -> optional(text),
        "lastDataOutputSize" -> optional(longNumber),
        "lastDuration" -> optional(longNumber))(Job.apply)(Job.unapply),
      "executables" -> list(mapping(
        "id" -> optional(number),
        "script" -> text,
        "jobId" -> number)(Executable.apply)(Executable.unapply)),
      "dependencies" -> list(number),
      "watchers" -> list(mapping(
        "id" -> optional(number),
        "jobId" -> number,
        "name" -> text,
        "email" -> text)(JobWatcher.apply)(JobWatcher.unapply)))(JobForm.apply)(JobForm.unapply))
}
