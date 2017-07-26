package controllers

import javax.inject._

import akka.actor.ActorSystem
import play.api.mvc._
import services.JobsService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class JobsController @Inject()(cc: ControllerComponents,
                               actorSystem: ActorSystem,
                               jobService: JobsService)(implicit exec: ExecutionContext) extends AbstractController(cc) {

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
