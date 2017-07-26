package Utils

import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder

/**
  * Created by tawkir on 7/26/2017.
  */
trait FakeApp {
  protected def configuredApp = new GuiceApplicationBuilder()
    .in(Mode.Test)
    .build
}
