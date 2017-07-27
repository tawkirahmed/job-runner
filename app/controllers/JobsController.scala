package controllers

import javax.inject._

import akka.actor.ActorSystem
import play.api.mvc._
import repositories.JobsRepository
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
    jobRepo.findAll().map(job => Ok(views.html.jobs(job)))
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

  // TODO: For future use the below code will work as an example
  private def getFutureMessage(delayTime: FiniteDuration): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      promise.success("Hi!")
    }(actorSystem.dispatcher)
    promise.future
  }

}
