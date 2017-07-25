package repositories

import javax.inject.Inject

import repositories.dtos.TableMappings.{ExecutablesTable, JobDependenciesTable, JobWatchersTable, JobsTable}
import repositories.dtos.dtos.{Executable, Job, JobWatcher}
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
class JobsRepository @Inject()(val config: DatabaseConfig[JdbcProfile])
  extends Db with JobsTable with JobDependenciesTable with ExecutablesTable with JobWatchersTable {

  import config.profile.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  def find(id: Int): Future[Option[Job]] = db.run((for (job <- jobs if job.id === id) yield job).result.headOption)

  def getExecutables: Seq[Executable] = {
    ???
  }

  def getJobDependencies: Future[Seq[(Int, Int)]] = {
    db.run((for (edge <- jobDependencies) yield edge.jobId -> edge.dependantJobId).result)
  }

  def getStartingJobs: Future[Seq[Job]] = {
    db.run {
      (for {
        (job, dependency) <- jobs joinLeft jobDependencies on (_.id === _.dependantJobId)
        if !dependency.isDefined && (!job.runTime.isDefined || true)
      } yield job).result
    }
  }

  def getJobDetails(jobIds: Seq[Int]): Future[Seq[(Job, Executable, JobWatcher)]] = {
    val query =
      for {
        job <- jobs if job.id.inSetBind(jobIds)
        executable <- executables if executable.jobId === job.id
        watcher <- jobWatchers if watcher.jobId === job.id
      } yield (job, executable, watcher)

    db.run {
      query.result
    }
  }

  /*
  CRUD Operation API
   */
  def insert(job: Job): Future[Job] = db
    .run(jobs returning jobs.map(_.id) += job)
    .map(id => job.copy(id = Some(id)))

  def init(): Future[Unit] = db.run(DBIOAction.seq(jobs.schema.create))

  def drop(): Future[Unit] = db.run(DBIOAction.seq(jobs.schema.drop))
}
