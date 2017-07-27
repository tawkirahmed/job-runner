package services

import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import Utils.CommonUtils
import akka.actor.ActorSystem
import com.typesafe.config.Config
import repositories.JobsRepository
import repositories.dtos.dtos._

import scala.async.Async.{async, await}
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
class JobsService @Inject()(
                             cfg: Config,
                             jobsRepo: JobsRepository,
                             jobScheduler: JobSchedulerService,
                             scriptingService: ScriptingService,
                             emailService: EmailService,
                             clock: Clock,
                             actorSystem: ActorSystem) {

  // TODO: For now jobs will be retrieved based on scheduled time when the run method gets invoked.
  def run = async {
    println("------------------------------------------------")
    val currentTime = CommonUtils.nowUTC(clock)

    val scheduledJobs = await(jobScheduler.getJobsRunOrder(currentTime))
    val jobWithDetails = await(getScheduledJobsDetails(scheduledJobs))
    val jobQueue = getJobQueue(jobWithDetails)

    while (!jobQueue.isEmpty) {
      val currentJobBatch = jobQueue.dequeue()
      try {
        println("Running batch with following jobs: " + currentJobBatch.map(_.job.name).mkString(", "))
        runJobBatch(currentJobBatch)
      } catch {
        case exception: Exception => {
          // TODO: may be retry
        }
      }
    }
  }

  private def runJob(job: JobDetails, jobExecutionId: String): JobInfo = {
    val startTime = System.currentTimeMillis()
    val outputSize = job.executables.foldLeft(0l)((outputSize, executable) => {
      try {
        outputSize + scriptingService.run(executable.script)
      } catch {
        case exception: Exception => {
          jobsRepo.insertJobExecution(JobExecution(jobExecutionId, job.job.id.get, executable.id.get, 4))
          throw exception
        }
      }
    })

    val endTime = System.currentTimeMillis()
    JobInfo(outputSize, (endTime - startTime))
  }

  private def updateJobStatus(job: Job, status: Int): Future[Int] = {
    val updatedJob = job.copy(status = status)
    jobsRepo.update(updatedJob)
  }

  private def runJobBatch(jobBatch: Seq[JobDetails]) = {
    jobBatch.foreach(job => {
      val executionId = CommonUtils.uuidString
      updateJobStatus(job.job, 2)
      try {
        val jobInfo = runJob(job, executionId)

        val updatedJob = job.job.copy(status = 3,
          lastRunTime = Option(CommonUtils.nowJavaDate(clock)),
          lastExecutionId = Option(executionId),
          lastDataOutputSize = Option(jobInfo.outputSize), lastDuration = Option(jobInfo.duration))
        jobsRepo.update(updatedJob)
      } catch {
        case exception: Exception => {
          val updatedJob = job.job.copy(status = 4,
            lastRunTime = Option(CommonUtils.nowJavaDate(clock)),
            lastExecutionId = Option(executionId))
          jobsRepo.update(updatedJob)
          emailService.sendMail(job.watchers.map(_.email), exception.getMessage)
          throw exception
        }
      }
    })
  }

  private def getJobQueue(scheduledJobs: Seq[Seq[JobDetails]]): mutable.Queue[Seq[JobDetails]] = {
    val queue = new mutable.Queue[Seq[JobDetails]]()
    scheduledJobs.foreach(jobs => queue.enqueue(jobs))
    queue
  }

  private def getScheduledJobsDetails(scheduledJobs: Seq[scala.Seq[Int]]): Future[Seq[Seq[JobDetails]]] = async {
    val jobDetails = await(getJobDetails(scheduledJobs.flatten))

    scheduledJobs.map(schedules => {
      schedules.map(jobId => {
        jobDetails.getOrElse(jobId, throw new Exception(s"Job details can not be build for job with id: $jobId"))
      })
    })
  }

  private def getJobDetails(jobIds: Seq[Int]): Future[Map[Int, JobDetails]] = async {
    await(jobsRepo.getJobDetails(jobIds))
      .groupBy(_._1.id.head)
      .map(x => {
        (x._1, JobDetails(x._2.map(_._1).head, x._2.map(_._2), x._2.map(_._3)))
      })
  }

  private case class JobInfo(outputSize: Long, duration: Long)

}
