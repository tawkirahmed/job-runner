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

    class Jobs(tag: Tag) extends Table[Job](tag, "jobs") {
      // Columns
      def id = column[Int]("JOB_ID", O.PrimaryKey, O.AutoInc)

      def name = column[String]("JOB_NAME", O.Length(512))

      def status = column[Int]("STATUS")

      def lastRunTime = column[Long]("LAST_RUN_TIME")

      def runTime = column[Option[Long]]("RUN_TIME")

      // Indexes
      def nameIndex = index("JOB_NAME_IDX", name, true)

      // Select
      def * = (id.?, name, status, lastRunTime, runTime) <> (Job.tupled, Job.unapply)
    }

    val jobs = TableQuery[Jobs]
  }

  trait ExecutablesTable extends JobsTable {
    this: Db =>

    import config.profile.api._

    class Executables(tag: Tag) extends Table[Executable](tag, "EXECUTABLES") {
      def id = column[Int]("EXECUTABLE_ID", O.PrimaryKey, O.AutoInc)

      def script = column[String]("JOB_NAME", O.Length(Int.MaxValue))

      // ForeignKey
      def jobId = column[Int]("JOB_ID")

      def jobFk = foreignKey("JOB_FK", jobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def * = (id.?, script, jobId) <> (Executable.tupled, Executable.unapply)
    }

    val executables = TableQuery[Executables]
  }

  trait JobDependenciesTable extends JobsTable {
    this: Db =>

    import config.profile.api._

    class JobDependencies(tag: Tag) extends Table[JobDependency](tag, "JOB_DEPENDENCIES") {
      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

      def jobId = column[Int]("JOB_ID")

      // ForeignKey
      def dependantJobId = column[Int]("DEPENDANT_JOB_ID")

      // TODO: Check foregin key action constraint
      def jobFk = foreignKey("JOB_FK", jobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def dJobFk = foreignKey("D_JOB_FK", dependantJobId, jobs)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def * = (id.?, jobId, dependantJobId) <> (JobDependency.tupled, JobDependency.unapply)
    }

    val jobDependencies = TableQuery[JobDependencies]
  }

}
