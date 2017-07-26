package controllers

import javax.inject._

import akka.actor.ActorSystem
import play.api.libs.json
import play.api.libs.json.Json
import play.api.mvc._
import repositories.JobsRepository
import repositories.dtos.dtos.Job
import services.JobsService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class JobsController @Inject()(cc: ControllerComponents,
                               actorSystem: ActorSystem,
                               jobService: JobsService,
                               jobRepo: JobsRepository
                              )(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def index = Action.async {
    jobRepo.findAll().map(x => Ok(views.html.jobs(x)))
  }

  def find(jobId: Int) = Action.async {
    jobRepo.find(jobId).map(x => Ok(views.html.jobDetails(x)))
  }

  def run = Action.async {
    jobService.run.map(x => Ok("Jobs has been started"))
  }

  // TODO: For future use the below code will work as an example
  private def getFutureMessage(delayTime: FiniteDuration): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      promise.success("Hi!")
    }(actorSystem.dispatcher)
    promise.future
  }

}
