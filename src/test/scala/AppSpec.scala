import com.google.inject.Guice
import org.scalatest.FlatSpec
import repositories.dtos.dtos.{Executable, Job, JobDependency, JobWatcher}
import repositories.{DbConfiguration, JobsRepository}
import services.JobsService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
class AppSpec extends FlatSpec with DbConfiguration {

  private lazy val injector = Guice.createInjector(
    new TestModule()
  )

  val jobService = injector.getInstance(classOf[JobsService])
  val jobRepo = injector.getInstance(classOf[JobsRepository])
  jobRepo.initAllTables()

  //
  var e1: Executable = null
  //

  "test case 1" should "work properly" in {
    val job = Await.result(jobRepo.insertJob(Job(id = None, name = "Job 1")), Duration.Inf)
    val ex = Await.result(jobRepo.insertExecutable(Executable(id = None,
      script = "echo script of job 1", job.id.get)), Duration.Inf)
    val watcher = Await.result(jobRepo.insertWatcher(JobWatcher(id = None,
      jobId = job.id.get, name = "Test User 1", email = "test@gmail.com")), Duration.Inf)

    Await.result(jobService.run, Duration.Inf)
  }

  "test case 2" should "work properly" in {
    val job1 = Await.result(jobRepo.insertJob(Job(id = None, name = "Job 2")), Duration.Inf)
    Await.result(jobRepo.insertExecutable(Executable(id = None,
      script = "echo script of job 2", job1.id.get)), Duration.Inf)
    Await.result(jobRepo.insertWatcher(JobWatcher(id = None,
      jobId = job1.id.get, name = "Test User 1", email = "test@gmail.com")), Duration.Inf)

    val job2 = Await.result(jobRepo.insertJob(Job(id = None, name = "Job 3")), Duration.Inf)
    e1 = Await.result(jobRepo.insertExecutable(Executable(id = None,
      script = "echo script of job 3", job2.id.get)), Duration.Inf)
    Await.result(jobRepo.insertWatcher(JobWatcher(id = None,
      jobId = job2.id.get, name = "Test User 1", email = "test@gmail.com")), Duration.Inf)

    Await.result(jobRepo.insertDependency(JobDependency(id = None, job1.id.get, job2.id.get)), Duration.Inf)

    Await.result(jobService.run, Duration.Inf)
  }

  "test case 3" should "work properly" in {
    val job = Await.result(jobRepo.insertJob(Job(id = None, name = "Job 4")), Duration.Inf)
    val ex = Await.result(jobRepo.insertExecutable(Executable(id = None,
      script = "echo script of job 4", job.id.get)), Duration.Inf)
    val watcher = Await.result(jobRepo.insertWatcher(JobWatcher(id = None,
      jobId = job.id.get, name = "Test User 1", email = "test@gmail.com")), Duration.Inf)


    Await.result(jobRepo.updateExecutable(e1.copy(script = "ERROR")), Duration.Inf)

    Await.result(jobService.run, Duration.Inf)
  }

  "test case 4" should "work properly" in {
    Await.result(jobRepo.updateExecutable(e1.copy(script = "echo script of job 3")), Duration.Inf)

    Await.result(jobService.run, Duration.Inf)
  }

}
