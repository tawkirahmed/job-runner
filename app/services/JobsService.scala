package services

import java.time.{Clock, ZonedDateTime}
import javax.inject.Inject

import Utils.CommonUtils
import akka.actor.ActorSystem
import com.typesafe.config.Config
import repositories.JobsRepository
import repositories.dtos.dtos._
import services.algorithms.DFS

import scala.async.Async.{async, await}
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
class JobsService @Inject()(
                             cfg: Config,
                             jobsRepo: JobsRepository,
                             scriptingService: ScriptingService,
                             emailService: EmailService,
                             dfs: DFS,
                             clock: Clock,
                             actorSystem: ActorSystem) {

  /**
    * This method first tries to build possible schedules of job for each independant job set
    * Then run each set one by one.
    * If one of the set failed executing at any point, the next will be executed without stopping as a whole
    *
    * @return
    */
  def run = async {
    val currentTime = CommonUtils.nowUTC(clock)

    val jobWithDetails = await(getJobsRunOrder(currentTime))
//    val jobWithDetails = await(getScheduledJobsDetails(currentTime))
    val jobQueue = getJobQueue(jobWithDetails)

    while (!jobQueue.isEmpty) {
      val currentJobBatch = jobQueue.dequeue()
      try {
        runJobBatch(currentJobBatch)
      } catch {
        case exception: Exception => {
        }
      }
    }
  }

  /**
    * Run a single job. It will not consider whether it's dependencies are resolved or not.
    *
    * @param jobId
    * @return
    */
  def run(jobId: Int) = {
    getJobDetails(jobId).map(jobDetails => {
      runJobBatch(Seq(jobDetails))
    })
  }

  def getJobsRunOrder(executionStartTime: ZonedDateTime): Future[Seq[Seq[JobDetails]]]  = async {
    val startingJobs = await(jobsRepo.getStartingJobs(executionStartTime))
    val edgeList = await(jobsRepo.getJobDependencies(executionStartTime)).toList

    val scheduledJobs = dfs.getTopSortWithIndependentNodes(startingJobs.map(_.id.get), edgeList)
    await(getScheduledJobsDetails(scheduledJobs))
  }

  /**
    * This method run executable scripts of each job and also keep track of the data generated.
    * If any scripts fail to execute then the execution of that job will terminate
    *
    * @param job
    * @param jobExecutionId
    * @return
    */
  private def runJob(job: JobDetails, jobExecutionId: String) = {
    Await.result(updateJobStatus(job.job, 2), Duration.Inf)
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
    val dataSize = (outputSize / 1024)
    val duration = (endTime - startTime) / (1000 * 60)
    JobInfo(dataSize, duration)
  }

  private def updateJobStatus(job: Job, status: Int): Future[Int] = {
    val updatedJob = job.copy(status = status)
    jobsRepo.update(updatedJob)
  }

  /**
    * This method run jobs that are scheduled in order of their dependency.
    * It will execute job one by one and if any of them fail then rest of the jobs which comes after it will not be executed.
    * This is not an optimized approach, in the README document an use case is documented where this method does not follow the most
    * optimized path.
    *
    * @param jobBatch
    */
  private def runJobBatch(jobBatch: Seq[JobDetails]) = {
    jobBatch.foreach(job => {
      val executionId = CommonUtils.uuidString
      try {

        val jobInfo = runJob(job, executionId)
        val updatedJob = job.job.copy(status = 3,
          lastRunTime = Option(CommonUtils.nowJavaDate(clock)),
          lastExecutionId = Option(executionId),
          lastDataOutputSize = Option(jobInfo.outputSize), lastDuration = Option(jobInfo.duration))
        Await.result(jobsRepo.update(updatedJob), Duration.Inf)
        verifyJobHealth(jobInfo, updatedJob, job.watchers)
      } catch {
        case exception: Exception => {
          val updatedJob = job.job.copy(status = 4,
            lastRunTime = Option(CommonUtils.nowJavaDate(clock)),
            lastExecutionId = Option(executionId))
          Await.result(jobsRepo.update(updatedJob), Duration.Inf)
          emailService.sendEmail(job.watchers.map(_.email), exception.getMessage)
          throw exception
        }
      }
    })
  }

  /**
    * Triggers email notifications, if some pre-defined validations failed.
    * This happens after a job has been executed successfully.
    *
    * @param jobInfo
    * @param job
    * @param watchers
    * @return
    */
  private def verifyJobHealth(jobInfo: JobInfo, job: Job, watchers: Seq[JobWatcher]) = {
    if (job.minimumDataOutputSize.isDefined && jobInfo.outputSize < job.minimumDataOutputSize.get) {
      emailService.sendEmail(watchers.map(_.email),
        s"The job: ${job.name} has produced less than defined minimum data limit.")
    }

    if (job.maximumDataOutputSize.isDefined && jobInfo.outputSize > job.maximumDataOutputSize.get) {
      emailService.sendEmail(watchers.map(_.email),
        s"The job: ${job.name} has produced more than defined maximum data limit.")
    }

    if (job.expectedDuration.isDefined && jobInfo.duration > job.expectedDuration.get) {
      emailService.sendEmail(watchers.map(_.email),
        s"The job: ${job.name} has took more than defined maximum job duration.")
    }
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

  def getJobDetails(jobId: Int): Future[JobDetails] = async {
    val detailMap = await(getJobDetails(Seq(jobId)))
    detailMap.getOrElse(jobId, throw new Exception("Jod Details could not be found!!!"))
  }

  def getJobDetails(jobIds: Seq[Int]): Future[Map[Int, JobDetails]] = async {
    await(jobsRepo.getJobDetails(jobIds))
      .groupBy(_._1.id.head)
      .map(x => {
        (x._1, JobDetails(x._2.map(_._1).head, x._2.map(_._2), x._2.flatMap(_._3), x._2.flatMap(_._4)))
      })
  }

  private case class JobInfo(outputSize: Long, duration: Long)

}
