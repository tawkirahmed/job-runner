package tasks

/**
  * Created by Tawkir Ahmed Fakir on 7/27/2017.
  */

import play.api.inject.{SimpleModule, _}

class TasksModule extends SimpleModule(bind[ScheduledTasks].toSelf.eagerly())
