package services

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

  def getJobsRunOrder(executionStartTime: Long): Future[Seq[Seq[Int]]] = async {
    val startingJobs = await(jobsRepo.getStartingJobs)
    val edgeList = await(jobsRepo.getJobDependencies).toList

    dfs.getTopSortWithIndependentNodes(startingJobs.map(_.id.get), edgeList)
  }
}
