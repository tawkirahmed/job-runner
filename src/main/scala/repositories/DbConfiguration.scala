package repositories

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
  * Created by Tawkir Ahmed Fakir on 7/22/2017.
  */
trait DbConfiguration {
  lazy val config = DatabaseConfig.forConfig[JdbcProfile]("db")
}

trait Db {
  val config: DatabaseConfig[JdbcProfile]
  val db = config.db
}
