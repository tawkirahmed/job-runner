import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
class JobRunnerModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Config]).toInstance(ConfigFactory.load())
  }
}
