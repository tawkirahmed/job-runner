package repositories

import javax.inject.Inject

import repositories.dtos.TableMappings.{JobDependenciesTable, JobsTable}
import repositories.dtos.dtos.{Executable, Job}
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
class JobsRepository @Inject()(val config: DatabaseConfig[JdbcProfile])
  extends Db with JobsTable with JobDependenciesTable {

  import config.profile.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  def find(id: Int) = db.run((for (job <- jobs if job.id === id) yield job).result.headOption)

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

  /*
  CRUD Operation API
   */
  def insert(job: Job): Future[Job] = db
    .run(jobs returning jobs.map(_.id) += job)
    .map(id => job.copy(id = Some(id)))

  def init() = db.run(DBIOAction.seq(jobs.schema.create))

  def drop() = db.run(DBIOAction.seq(jobs.schema.drop))
}
