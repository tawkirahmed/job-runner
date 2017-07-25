package services

import java.time.ZonedDateTime
import javax.inject.Inject

import com.typesafe.config.Config
import repositories.JobsRepository
import services.algorithms.DFS

import scala.async.Async.{async, await}
import scala.concurrent.Future

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
class JobSchedulerService @Inject()(cfg: Config, jobsRepo: JobsRepository, dfs: DFS) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getJobsRunOrder(executionStartTime: ZonedDateTime): Future[Seq[Seq[Int]]] = async {
    val startingJobs = await(jobsRepo.getStartingJobs)
    val edgeList = await(jobsRepo.getJobDependencies).toList

    val scheduledJobs = dfs.getTopSortWithIndependentNodes(startingJobs.map(_.id.get), edgeList)
    getscheduledJobsDetails(scheduledJobs)
  }

  private def getscheduledJobsDetails(scheduledJobs: Seq[scala.Seq[Int]]) = {

    val jobIds = scheduledJobs.flatten
    getJobDetails(jobIds)
    ???
  }

  private def getJobDetails(jobIds: Seq[Int]) = {

  }
}
