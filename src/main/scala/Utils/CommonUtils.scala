package Utils

import java.time.{Clock, ZoneId, ZonedDateTime}

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
object CommonUtils {
  private val Z = ZoneId.of("Z")

  def nowUTC(clock: Clock = Clock.systemUTC()) = ZonedDateTime.now(clock.withZone(Z))

}
