package repositories

import org.scalatest.FlatSpec
import repositories.dtos.dtos.Job

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
class JobsRepositorySpec extends FlatSpec with DbConfiguration {
  val repo = new JobsRepository(config)
  repo.init()

  "insert" should "add new data successfully" in {
    val job = Await.result(repo.insert(Job(id = None, name = "First Job", status = 1)), Duration.Inf)
    assert(job.name == "First Job")
  }
}
