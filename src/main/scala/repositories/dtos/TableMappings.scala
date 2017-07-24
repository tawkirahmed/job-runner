package repositories.dtos

import repositories.Db
import repositories.dtos.dtos.{Executable, Job, JobDependency}

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
object TableMappings {

  trait JobsTable {
    this: Db =>

    import config.profile.api._

    val jobs = TableQuery[Jobs]

    class Jobs(tag: Tag) extends Table[Job](tag, "jobs") {
      // Indexes
      def nameIndex = index("JOB_NAME_IDX", name, true)

      def name = column[String]("JOB_NAME", O.Length(512))

      // Select
      def * = (id.?, name, status, lastRunTime, runTime) <> (Job.tupled, Job.unapply)

      // Columns
      def id = column[Int]("JOB_ID", O.PrimaryKey, O.AutoInc)

      def status = column[Int]("STATUS")

      def lastRunTime = column[Long]("LAST_RUN_TIME")

      def runTime = column[Option[Long]]("RUN_TIME")
    }

  }

  trait ExecutablesTable extends JobsTable {
    this: Db =>

    import config.profile.api._

    val executables = TableQuery[Executables]

    class Executables(tag: Tag) extends Table[Executable](tag, "EXECUTABLES") {
      def jobFk = foreignKey("JOB_FK", jobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      // ForeignKey
      def jobId = column[Int]("JOB_ID")

      def * = (id.?, script, jobId) <> (Executable.tupled, Executable.unapply)

      def id = column[Int]("EXECUTABLE_ID", O.PrimaryKey, O.AutoInc)

      def script = column[String]("JOB_NAME", O.Length(Int.MaxValue))
    }

  }

  trait JobDependenciesTable extends JobsTable {
    this: Db =>

    import config.profile.api._

    val jobDependencies = TableQuery[JobDependencies]

    class JobDependencies(tag: Tag) extends Table[JobDependency](tag, "JOB_DEPENDENCIES") {
      // TODO: Check foregin key action constraint
      def jobFk = foreignKey("JOB_FK", jobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def jobId = column[Int]("JOB_ID")

      def dJobFk = foreignKey("D_JOB_FK", dependantJobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def * = (id.?, jobId, dependantJobId) <> (JobDependency.tupled, JobDependency.unapply)

      // ForeignKey
      def dependantJobId = column[Int]("DEPENDANT_JOB_ID")

      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    }
  }
}

