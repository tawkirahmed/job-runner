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


  def getJobsRunOrder(executionStartTime: Long): Future[scala.Seq[Int]] = async {
    val startingJobs = await(jobsRepo.getStartingJobs)
    val edgeMap = await(jobsRepo.getJobDependencies)

    dfs.getTopSort(startingJobs.map(_.id.get), edgeMap)
  }
}
