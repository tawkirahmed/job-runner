package repositories.dtos

import java.sql.Date

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
object dtos {

  case class JobDetails(job: Job, executables: Seq[Executable], watchers: Seq[JobWatcher])

  // TODO: make staus an enum. For now the mapping is as follows
  // 1 -> idle, 2 -> running, 3 -> completed, 4 -> error
  case class Job(id: Option[Int], name: String, status: Int = 1,
                 lastRunTime: Option[Date] = None, runTime: Option[Long] = None,
                 minimumDataOutputSize: Option[Long] = None, // in kilobytes
                 maximumDataOutputSize: Option[Long] = None, // in kilobytes
                 expectedDuration: Option[Long] = None, // in minutes
                 lastExecutionId: Option[String] = None,
                 lastDataOutputSize: Option[Long] = None, // in kilobytes
                 lastDuration: Option[Long] = None // in minutes
                )

  // TODO: Keeping executable to job as a one to many mapping but it may be a many to many mapping also
  // avoiding complexities for now
  // if later on we make it as many to many mapping then the jobs will be able to share same scripts
  case class Executable(id: Option[Int], script: String, jobId: Int)

  case class JobDependency(id: Option[Int], jobId: Int, dependantJobId: Int)

  // This class will hold information regarding every execution of jobs. We may clean the table from time to time
  // 1 -> idle, 2 -> running, 3 -> completed, 4 -> error
  case class JobExecution(id: String, jobId: Int, executableId: Int, status: Int)

  // Emails will be sent to the watchers of a job
  case class JobWatcher(id: Option[Int], jobId: Int, name: String, email: String)

}
