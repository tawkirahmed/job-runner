package repositories.dtos

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
object dtos {

  // TODO: make staus an enum. For now the mapping is as follows
  // 1 -> idle, 2 -> running, 3 -> completed, 4 -> error
  case class Job(id: Option[Int], name: String, status: Int, lastRunTime: Long = 0, runTime: Option[Long] = None)

  // TODO: Keeping executable to job as a one to many mapping but it may be a many to many mapping also
  // avoiding complexities for now
  case class Executable(id: Option[Int], script: String, jobId: Int)

  case class JobDependency(id: Option[Int], jobId: Int, dependantJobId: Int)

}
