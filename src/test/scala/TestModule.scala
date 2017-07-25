import java.time.Clock

import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}
import repositories.{DbConfiguration, JobsRepository}
import services.{JobSchedulerService, ScriptingService}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
class TestModule extends AbstractModule with DbConfiguration {

  override def configure(): Unit = {
    bind(classOf[Config]).toInstance(ConfigFactory.load())
    bind(classOf[JobsRepository]).toInstance(new JobsRepository(config))
    bind(classOf[DatabaseConfig[JdbcProfile]]).toInstance(config)
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone())
  }

}
