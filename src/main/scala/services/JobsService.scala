package services

import java.time.Clock
import javax.inject.Inject

import Utils.CommonUtils
import com.typesafe.config.Config
import repositories.JobsRepository
import repositories.dtos.dtos.JobDetails

import scala.async.Async.{async, await}
import scala.collection.mutable
import scala.concurrent.Future

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
class JobsService @Inject()(
                             cfg: Config,
                             jobsRepo: JobsRepository,
                             jobScheduler: JobSchedulerService,
                             clock: Clock) {

  import scala.concurrent.ExecutionContext.Implicits.global

  // TODO: For now jobs will be retrieved based on scheduled time when the run method gets invoked.
  def run = async {
    val currentTime = CommonUtils.nowUTC(clock)

    val scheduledJobs = await(jobScheduler.getJobsRunOrder(currentTime))
    val jobWithDetails = await(getScheduledJobsDetails(scheduledJobs))
    val jobQueue = getJobQueue(jobWithDetails)

    while (!jobQueue.isEmpty) {
      val currentJobBatch = jobQueue.dequeue()
      try {
        runJobBatch(currentJobBatch)
      }
      catch {
        case _ => {
          // retry
          // send email
          // resume
        }
      }
    }
  }

  def runJobBatch(jobBatch: Seq[JobDetails]) = {
    jobBatch.foreach(job => {
      try {
        runJob(job)
      } catch {
        // update processing state in db
        case _ => throw new Exception()
      }
    })

  }

  def runJob(job: JobDetails): Unit = {

  }

  private def getJobQueue(scheduledJobs: Seq[Seq[JobDetails]]): mutable.Queue[Seq[JobDetails]] = {
    val queue = new mutable.Queue[Seq[JobDetails]]()
    scheduledJobs.foreach(jobs => queue.enqueue(jobs))
    queue
  }

  private def getScheduledJobsDetails(scheduledJobs: Seq[scala.Seq[Int]])
  : Future[Seq[Seq[JobDetails]]] = async {
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
}
