package Utils

import java.sql.Date
import java.time.{Clock, ZoneId, ZonedDateTime}
import java.util.UUID

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
object CommonUtils {
  private val Z = ZoneId.of("Z")

  def nowUTC(clock: Clock = Clock.systemUTC()) = ZonedDateTime.now(clock.withZone(Z))

  def nowJavaDate(clock: Clock = Clock.systemUTC()) = {
    new Date(ZonedDateTime.now(clock.withZone(Z)).toInstant().getEpochSecond * 1000l)
  }

  def uuidString = UUID.randomUUID().toString

  def currentTimeLong = {
    val now = nowUTC()
    val timeOnly = (now.getHour * 60 * 60) +
      (now.getMinute * 60) +
      now.getSecond

    timeOnly
  }

}
