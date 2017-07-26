package services

import java.time.ZonedDateTime
import javax.inject.Inject

import com.typesafe.config.Config
import repositories._
import services.algorithms.DFS

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
class JobSchedulerService @Inject()(cfg: Config, jobsRepo: JobsRepository, dfs: DFS) {

  def getJobsRunOrder(executionStartTime: ZonedDateTime): Future[Seq[Seq[Int]]] = async {
    val startingJobs = await(jobsRepo.getStartingJobs)

    println("Starting jobs number " + startingJobs.size)
    val edgeList = await(jobsRepo.getJobDependencies).toList

    dfs.getTopSortWithIndependentNodes(startingJobs.map(_.id.get), edgeList)
  }
}