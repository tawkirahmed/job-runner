package repositories.dtos

import repositories.Db
import repositories.dtos.dtos._

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
object TableMappings {

  //TODO: Need to correct the foreign key mappings

  trait JobsTable {
    this: Db =>

    import config.profile.api._

    val jobs = TableQuery[Jobs]

    class Jobs(tag: Tag) extends Table[Job](tag, "jobs") {
      // Columns
      def id = column[Int]("JOB_ID", O.PrimaryKey, O.AutoInc)

      def name = column[String]("JOB_NAME", O.Length(512))

      def status = column[Int]("STATUS")

      def lastRunTime = column[Long]("LAST_RUN_TIME")

      def runTime = column[Option[Long]]("RUN_TIME")

      def minimumDataOutputSize = column[Option[Long]]("MINIMUM_DATA_OUTPUT_SIZE")

      def maximumDataOutputSize = column[Option[Long]]("MAXIMUM_DATA_OUTPUT_SIZE")

      def expectedDuration = column[Option[Long]]("EXPECTED_DURATION")

      def lastExecutionId = column[Option[String]]("LAST_EXECUTION_ID")

      def lastDataOutputSize = column[Option[Long]]("LAST_DATA_OUTPUT_SIZE")

      def lastDuration = column[Option[Long]]("LAST_DURATION")

      // Indexes
      def nameIndex = index("JOB_NAME_IDX", name, false)

      // Select
      def * = (id.?, name, status, lastRunTime, runTime, minimumDataOutputSize,
        maximumDataOutputSize, expectedDuration,
        lastExecutionId, lastDataOutputSize, lastDuration) <> (Job.tupled, Job.unapply)
    }

  }

  trait ExecutablesTable extends JobsTable {
    this: Db =>

    import config.profile.api._

    val executables = TableQuery[Executables]

    class Executables(tag: Tag) extends Table[Executable](tag, "EXECUTABLES") {
      def id = column[Int]("EXECUTABLE_ID", O.PrimaryKey, O.AutoInc)

      def jobId = column[Int]("JOB_ID")

      def script = column[String]("JOB_NAME", O.Length(Int.MaxValue))

      // ForeignKey
      def jobFk = foreignKey("JOB_FK", jobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def * = (id.?, script, jobId) <> (Executable.tupled, Executable.unapply)
    }

  }

  trait JobDependenciesTable extends JobsTable {
    this: Db =>

    import config.profile.api._

    val jobDependencies = TableQuery[JobDependencies]

    class JobDependencies(tag: Tag) extends Table[JobDependency](tag, "JOB_DEPENDENCIES") {
      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

      def jobId = column[Int]("JOB_ID")

      def dependantJobId = column[Int]("DEPENDANT_JOB_ID")

      // TODO: Check foregin key action constraint
      // ForeignKey
      def jobFk = foreignKey("JOB_FK", jobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def dJobFk = foreignKey("D_JOB_FK", dependantJobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def * = (id.?, jobId, dependantJobId) <> (JobDependency.tupled, JobDependency.unapply)
    }

  }

  trait JobExecutionsTable extends JobsTable with ExecutablesTable {
    this: Db =>

    import config.profile.api._

    val jobExecutions = TableQuery[JobExecutions]

    class JobExecutions(tag: Tag) extends Table[JobExecution](tag, "JOB_EXECUTIONS") {
      def id = column[String]("ID")

      def jobId = column[Int]("JOB_ID")

      def executableId = column[Int]("EXECUTABLE_ID")

      def status = column[Int]("STATUS")

      // TODO: Check foregin key action constraint
      // ForeignKey
      def jobFk = foreignKey("JOB_FK", jobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def executableFk = foreignKey("EXECUTABLE_FK", executableId, executables)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def * = (id, jobId, executableId, status) <> (JobExecution.tupled, JobExecution.unapply)
    }

  }

  trait JobWatchersTable extends JobsTable with ExecutablesTable {
    this: Db =>

    import config.profile.api._

    val jobWatchers = TableQuery[JobWatchers]

    class JobWatchers(tag: Tag) extends Table[JobWatcher](tag, "JOB_WATCHERS") {
      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

      def jobId = column[Int]("JOB_ID")

      def name = column[String]("NAME", O.Length(512))

      def email = column[String]("EMAIL", O.Length(512))

      // TODO: Check foregin key action constraint
      // ForeignKey
      def jobFk = foreignKey("JOB_FK", jobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def * = (id.?, jobId, name, email) <> (JobWatcher.tupled, JobWatcher.unapply)
    }

  }

}

