package services

import java.time.Clock
import javax.inject.Inject

import com.typesafe.config.Config
import repositories.JobsRepository

/**
  * Created by Tawkir Ahmed Fakir on 7/25/2017.
  */
// TODO: May be this is not needed at all
class CRUDService @Inject()(
                             jobsRepo: JobsRepository) {
  def initAllTables(): Unit = {
    jobsRepo.initAllTables()
  }

}
