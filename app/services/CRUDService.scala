package services

import javax.inject.Inject

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
