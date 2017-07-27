package tasks

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.actor.ActorSystem
import com.typesafe.config.Config
import services.JobsService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by Tawkir Ahmed Fakir on 7/27/2017.
  */
class ScheduledTasks @Inject()(
                                cfg: Config,
                                actorSystem: ActorSystem,
                                jobService: JobsService)(implicit executionContext: ExecutionContext) {


  val delay = getDelay

  if (cfg.getBoolean("app.schedule.enabled")) {
    actorSystem.scheduler.schedule(initialDelay = Duration(1, TimeUnit.SECONDS), interval = delay) {
      jobService.run
    }
  }


  private def getDelay: FiniteDuration = {
    val delayString = cfg.getString("app.schedule.interval").split("-")
    val (delayUnit, delayFormat) = (delayString(0).toInt, delayString(1))

    delayFormat match {
      case "s" => Duration(delayUnit, TimeUnit.SECONDS)
      case "m" => Duration(delayUnit, TimeUnit.MINUTES)
      case "h" => Duration(delayUnit, TimeUnit.HOURS)
      case "d" => Duration(delayUnit, TimeUnit.DAYS)
      case "M" => Duration(delayUnit * 30, TimeUnit.DAYS)
    }
  }
}
