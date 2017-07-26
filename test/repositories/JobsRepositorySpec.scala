package repositories

import Utils.FakeApp
import org.scalatest.FlatSpec
import repositories.dtos.dtos.Job

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
class JobsRepositorySpec extends FlatSpec with FakeApp {

  val jobRepo = configuredApp.injector.instanceOf(classOf[JobsRepository])
  jobRepo.initAllTables()

  "insert" should "add new data successfully" in {
    val job = Await.result(jobRepo.insertJob(Job(id = None, name = "First Job", status = 1)), Duration.Inf)
    assert(job.name == "First Job")
  }
}
