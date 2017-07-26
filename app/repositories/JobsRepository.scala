package repositories

import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.dtos.TableMappings
import repositories.dtos.dtos._
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
class JobsRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends TableMappings with HasDatabaseConfigProvider[JdbcProfile] {

  import dbConfig.profile.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  def find(id: Int): Future[Option[Job]] = db.run((for (job <- jobs if job.id === id) yield job).result.headOption)

  def getExecutables: Seq[Executable] = {
    ???
  }

  def getJobDependencies: Future[Seq[(Int, Int)]] = {
    // TODO: Need to add completed depency
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
        watcher <- jobWatchers if watcher.jobId === job.id //TODO: Make it left join
      } yield (job, executable, watcher)

    db.run {
      query.result
    }
  }

  /*
  CRUD Operation API
   */
  def insertJob(job: Job): Future[Job] = db
    .run(jobs returning jobs.map(_.id) += job)
    .map(id => job.copy(id = Some(id)))

  def insertExecutable(ex: Executable): Future[Executable] = db
    .run(executables returning executables.map(_.id) += ex)
    .map(id => ex.copy(id = Some(id)))

  def insertWatcher(watcher: JobWatcher): Future[JobWatcher] = db
    .run(jobWatchers returning jobWatchers.map(_.id) += watcher)
    .map(id => watcher.copy(id = Some(id)))

  def insertDependency(dependency: JobDependency): Future[JobDependency] = db
    .run(jobDependencies returning jobDependencies.map(_.id) += dependency)
    .map(id => dependency.copy(id = Some(id)))

  def update(job: Job): Future[Int] = db.run(jobs.filter(_.id === job.id).update(job))

  def updateExecutable(ex: Executable): Future[Int] = db.run(executables.filter(_.id === ex.id).update(ex))

  def insertJobExecution(jobExecution: JobExecution): Future[JobExecution] = db
    .run(jobExecutions returning jobExecutions += jobExecution)

  def initAllTables(): Future[Unit] = {
    // TODO: https://stackoverflow.com/questions/33929709/scala-slick-how-to-create-schema-only-if-it-does-not-exist

    db.run(DBIOAction.seq(jobs.schema.create))
    db.run(DBIOAction.seq(executables.schema.create))
    db.run(DBIOAction.seq(jobDependencies.schema.create))
    db.run(DBIOAction.seq(jobExecutions.schema.create))
    db.run(DBIOAction.seq(jobWatchers.schema.create))
  }

  def drop(): Future[Unit] = db.run(DBIOAction.seq(jobs.schema.drop))
}
