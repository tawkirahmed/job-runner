package repositories

import java.sql.Date
import java.time.ZonedDateTime
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

  initAllTables() // TODO: Remove, this should be taken care of by the deployment script

  /**
    * This is the most straight forward query, we finding a job based on its id.
    *
    * @param id
    * @return
    */
  def find(id: Int): Future[Job] = db.run((for (job <- jobs if job.id === id) yield job).result.head)

  /**
    * Find all jobs, this has several. Specially when user pick dependecy.
    *
    * @return
    */
  def findAll(): Future[Seq[Job]] = {
    db.run((for (job <- jobs) yield job).result)
  }

  /**
    * The dependency table holds two columns which maintain the relation between the dependant job
    * jobId denotes the job which must be completed first
    * dependantJobId is the job which will be eligable to run after the Job with jobId is completed.
    * We consider a dependency exists if
    *   1. There is dependency relation defined by user AND
    *   2. The parent job (with the jobId) is not completed OR the parent job has never run OR the parent job has previously run but not today
    * @param executionStartTime
    * @return
    */
  def getJobDependencies(executionStartTime: ZonedDateTime): Future[Seq[(Int, Int)]] = {
    val date = new Date(executionStartTime.toInstant().getEpochSecond * 1000l) // converting current time to java.sql.date
    db.run((for {
      edge <- jobDependencies
      job <- jobs if (job.id === edge.jobId && (job.status =!= 3 || !job.lastRunDate.isDefined || job.lastRunDate < date))
    } yield edge.jobId -> edge.dependantJobId).result)
  }

  /**
    * This method finds the root jobs which have no dependency, those will work as a starting point.
    * Possible candidates have following properties:
    *   1. They have no dependency and they are scheduled to run today
    *   2. OR they failed in the last try TODAY, that means all their dependency has already been completed for today.
    * @param executionStartTime
    * @return
    */
  def getStartingJobs(executionStartTime: ZonedDateTime): Future[Seq[Job]] = {
    val date = new Date(executionStartTime.toInstant().getEpochSecond * 1000l)
    db.run {
      (for {
        (job, dependency) <- jobs joinLeft jobDependencies on (_.id === _.dependantJobId)
        if (
          (!dependency.isDefined && !job.runTime.isDefined &&
            (!job.lastRunDate.isDefined || job.lastRunDate < date))
            || (job.status === 4 && job.lastRunDate === date))
      } yield job).result
    }
  }

  /**
    * This method finds all details of a set of Jobs which will be used during running the job
    * @param jobIds
    * @return
    */
  def getJobDetails(jobIds: Seq[Int]): Future[Seq[(Job, Executable, Option[JobWatcher], Option[Job])]] = {
    val q1 =
      for {
        job <- jobs  if job.id.inSetBind(jobIds)
        executable <- executables if executable.jobId === job.id
      } yield (job, executable)

    val q2 = for {
      (jobWithExecutable, watcher) <- q1 joinLeft jobWatchers on (_._1.id === _.jobId)
    } yield (jobWithExecutable._1, jobWithExecutable._2, watcher)

    val q3 = for {
      (jew, dependencies) <- q2 joinLeft jobDependencies on (_._1.id === _.dependantJobId)
    } yield (jew._1, jew._2, jew._3, dependencies)

    val q4 = for {
      (jewd, dj) <- q3 joinLeft jobs on (_._4.map(_.jobId) === _.id)
    } yield (jewd._1, jewd._2, jewd._3, dj)

    db.run {
      q4.result
    }
  }

  /**
    * The following methods are the CRUD apis
    **/

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

  def insertJobExecution(jobExecution: JobExecution): Future[JobExecution] = db
    .run(jobExecutions returning jobExecutions += jobExecution)

  def update(job: Job): Future[Int] = db.run(jobs.filter(_.id === job.id).update(job))

  def updateWatchers(watcher: JobWatcher): Future[Int] = db.run(jobWatchers.filter(_.id === watcher.id).update(watcher))

  def delete(jobId: Int): Future[Int] = {
    db.run(jobs.filter(_.id === jobId).delete)
  }

  def deleteDependenciesOf(id: Int): Future[Int] = {
    db.run(jobDependencies.filter(_.dependantJobId === id).delete)
  }

  def updateExecutable(ex: Executable): Future[Int] = db.run(executables.filter(_.id === ex.id).update(ex))

  def initAllTables(): Future[Unit] = {
    db.run(DBIOAction.seq(jobs.schema.create))
    db.run(DBIOAction.seq(executables.schema.create))
    db.run(DBIOAction.seq(jobDependencies.schema.create))
    db.run(DBIOAction.seq(jobExecutions.schema.create))
    db.run(DBIOAction.seq(jobWatchers.schema.create))
  }

  def drop(): Future[Unit] = db.run(DBIOAction.seq(jobs.schema.drop))
}
