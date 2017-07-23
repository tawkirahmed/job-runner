import com.google.inject.Guice
import services.JobSchedulerService

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
object JobRunner {
  def main(args: Array[String]): Unit = {
    injector.getInstance(classOf[JobSchedulerService])
    println("Hello World!!!")

  }

  private lazy val injector = Guice.createInjector(
    new JobRunnerModule()
  )
}
