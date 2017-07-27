package controllers

import javax.inject._

import akka.actor.ActorSystem
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import repositories.JobsRepository
import repositories.dtos.dtos.{Executable, Job, JobDependency, JobWatcher}
import services.JobsService

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JobsController @Inject()(cc: ControllerComponents,
                               messagesApi: MessagesApi,
                               actorSystem: ActorSystem,
                               jobService: JobsService,
                               jobRepo: JobsRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  import JobsController._

  def index = Action.async {
    jobRepo.findAll().map(job => Ok(views.html.jobs(job)))
  }

  def find(jobId: Int) = Action.async {
    jobRepo.find(jobId).map(x => Ok(views.html.jobDetails(x)))
  }

  def add() = Action.async { implicit request: Request[AnyContent] =>
    getJobList.map(allJobs => Ok(views.html.jobFormPage(jobForm, allJobs)))
  }

  def update(jobId: Int) = Action.async { implicit request: Request[AnyContent] =>
    jobService.getJobDetails(jobId).flatMap(jobDetails => {
      val filledForm = jobForm.fill(JobForm(
        id = jobDetails.job.id,
        name = jobDetails.job.name,
        // TODO: run time add
        //        runTime = jobDetails.job.runTime,
        minDataOutSize = jobDetails.job.minimumDataOutputSize,
        maxDataOutSize = jobDetails.job.maximumDataOutputSize,
        expectedTime = jobDetails.job.expectedDuration,
        script = jobDetails.executables.head.script,
        dependencies = jobDetails.parentJobs.map(_.id.get).toList,
        watcher = jobDetails.watchers.headOption.map(_.email)
      ))

      getJobList.map(allJobs => Ok(views.html.jobFormPage(filledForm, allJobs)))
    })
  }

  def save = Action.async(parse.formUrlEncoded(maxLength = 10000000)) { implicit req =>
    jobForm.bindFromRequest.fold(
      formWithErrors => {
        getJobList.map(allJobs => BadRequest(views.html.jobFormPage(formWithErrors, allJobs)))
      },
      jobInput => {
        val updatedJobId = if (jobInput.id.isDefined) updateJob(jobInput) else saveJob(jobInput)
        updatedJobId.map(id => Redirect(routes.JobsController.find(id)))
      }
    )
  }

  def delete(jobId: Int) = Action.async {
    jobRepo.delete(jobId).map(x => {
      Redirect(routes.JobsController.index)
    })
  }

  def runAll = Action.async {
    jobService.run.map(_ =>
      Redirect(routes.JobsController.index)
    )
  }

  def run(jobId: Int) = Action.async {
    jobService.run(jobId).map(_ =>
      Redirect(routes.JobsController.index)
    )
  }

  def clearLastRun(jobId: Int) = Action.async {
    jobRepo.find(jobId).flatMap(
      job => {
        jobRepo.update(job.copy(lastRunTime = None)).map(_ => Redirect(routes.JobsController.index))
      }
    )
  }

  def clearLastRunAll() = Action.async {
    jobRepo.findAll().flatMap(jobs =>
      Future.sequence(jobs.map(job => {
        val updatedJob = job.copy(lastRunTime = None)
        jobRepo.update(updatedJob).map(_ => updatedJob)
      })).map(_ => Redirect(routes.JobsController.index)))
  }

  private def getJobList = {
    jobRepo.findAll().map(jobs => {
      jobs.map(job => (job.id.get.toString -> job.name))
    })
  }

  private def saveJob(jobInput: JobForm) = async {
    // TODO: Add run time
    val job = Job(name = jobInput.name, minimumDataOutputSize = jobInput.minDataOutSize
      , maximumDataOutputSize = jobInput.maxDataOutSize, expectedDuration = jobInput.expectedTime)

    val createdJob = await {
      jobRepo.insertJob(job)
    }

    jobRepo.insertExecutable(Executable(script = jobInput.script, jobId = createdJob.id.get))

    jobInput.dependencies.foreach(parentJobId => {
      jobRepo.insertDependency(JobDependency(jobId = parentJobId, dependantJobId = createdJob.id.get))
    })

    jobInput.watcher.map(watcher => {
      jobRepo.insertWatcher(JobWatcher(jobId = createdJob.id.get, name = "", email = watcher))
    })

    createdJob.id.get
  }

  private def updateJob(jobInput: JobForm) = async {

    val jobDetails = await(jobService.getJobDetails(jobInput.id.get))

    // TODO: Add run time
    val job = jobDetails.job.copy(name = jobInput.name, minimumDataOutputSize = jobInput.minDataOutSize
      , maximumDataOutputSize = jobInput.maxDataOutSize, expectedDuration = jobInput.expectedTime)

    await {
      jobRepo.update(job)
    }

    val updatedJobId = job.id.get

    jobRepo.updateExecutable(jobDetails.executables.head.copy(script = jobInput.script, jobId = updatedJobId))

    await(jobRepo.deleteDependenciesOf(updatedJobId))

    jobInput.dependencies.foreach(parentJobId => {
      jobRepo.insertDependency(JobDependency(jobId = parentJobId, dependantJobId = updatedJobId))
    })

    jobInput.watcher.map(watcher => {
      jobRepo.updateWatchers(jobDetails.watchers.head.copy(jobId = updatedJobId, email = watcher))
    })

    updatedJobId
  }
}

object JobsController {

  // TODO: Need to add support for multiple script and watcher
  case class JobForm(id: Option[Int], name: String,
                     runTime: Option[String] = None,
                     minDataOutSize: Option[Long], maxDataOutSize: Option[Long], expectedTime: Option[Long]
                     , script: String, dependencies: List[Int], watcher: Option[String])

  val jobForm: Form[JobForm] = Form(
    mapping("id" -> optional(number),
      "name" -> nonEmptyText,
      "runTime" -> optional(text),
      "minDataOutSize" -> optional(longNumber),
      "maxDataOutSize" -> optional(longNumber),
      "expectedTime" -> optional(longNumber),
      "script" -> nonEmptyText,
      "dependencies" -> list(number),
      "watchers" -> optional(email))(JobForm.apply)(JobForm.unapply)
  )
}
