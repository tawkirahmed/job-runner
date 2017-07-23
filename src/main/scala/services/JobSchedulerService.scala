package services

import javax.inject.Inject

import com.typesafe.config.Config
import repositories.JobsRepository
import repositories.dtos.dtos.Job

import scala.async.Async.{async, await}

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
class JobSchedulerService @Inject()(cfg: Config, jobsRepo: JobsRepository) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getJobsRunOrder(executionStartTime: Long) = async {
    var startingJobs = await(jobsRepo.getStartingJobs).toList
    val edgeList = await(jobsRepo.getJobDependencies)

    var jobOrder = List.empty[Job]
    while (startingJobs.isEmpty == false) {
      val node = startingJobs.head
      startingJobs = startingJobs.tail
      jobOrder = jobOrder :+ node
      edgeList(node.id.head).foreach(dependantNode => {

      })
    }

    edgeList

  }


}
