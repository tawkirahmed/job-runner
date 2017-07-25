package services

import java.time.Clock
import javax.inject.Inject

import Utils.CommonUtils
import com.typesafe.config.Config
import repositories.JobsRepository

import scala.async.Async.{async, await}
import scala.collection.mutable

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
    val jobQueue = getJobQueue(scheduledJobs)

    while (!jobQueue.isEmpty) {
      val currentJobBatch = jobQueue.dequeue()
    }
  }

  private def getJobQueue(scheduledJobs: Seq[Seq[Int]]): mutable.Queue[Seq[Int]] = {
    val queue = new mutable.Queue[Seq[Int]]()
    scheduledJobs.foreach(jobs => queue.enqueue(jobs))
    queue
  }
}
