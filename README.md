# job-runner

###### Installation instructions:

1. Run the sql file: **release/scripts/init.sql**. This will create the database and necessary tables.

1. To populate data, run the sql: **release/scripts/data_gen.sql**.

1. Unzip the file: **release/job_runner-1.0.zip**. Please make sure that java 8 runtime is installed in the machine.

2. Then run the script in the **bin** directory of the extracted files. Script location is **bin/job_runner**.
    1. For Unix users, zip files do not retain Unix file permissions so when the file is expanded the start script will be required to be set as an executable: $ chmod +x /path/to/bin/<project-name>

3. Now the application UI can be accessible from: **http://localhost:9000/**.


###### Application configurations:

1. The application config file resides in: **release/job_runner-1.0/conf/application.conf**

2. **app.schedule.enabled** setting this value to **true** will trigger the job runner at a configured interval.

 3. **app.schedule.interval** this value determines the interval. The possible values can be seen in the comment of the conf file.

 4. To configure the database these configurations need to be updated: **slick.dbs.default.db**. The correct mysql database name, user name and password need to be provided here.

 5. Similarly for email credentials, **play.mailer** settings need to be updated.
 
 
###### Tech stacks:
1. Core framework: Play - https://www.playframework.com/

2. Language: Scala - https://www.scala-lang.org/

3. Relational mapper for mysql: Slick - http://slick.lightbend.com/

4. Test framework: Scala test - http://www.scalatest.org/


###### Implementation details:

1. The challenge in this project is to execute the jobs in such a way that, all the parent jobs on which a job depends gets executed before-hand.
2. To address this problem following approach has been taken.
      1. At first identify the jobs which does not have any dependency. These are our root jobs.
      2. Now the job dependency relation can be thought as a directed graph. As cycle is not an option so the dependency can be represented in one or more directed acyclic graph (DAG).
      3. We can safely assume that individual DAG has no dependency with other DAG. So our first goal is to identify all these independent graphs.
            1. To do that, we make the job dependency relation uni directional.
            2. Then from the root jobs, a simple DFS traversal can mark the jobs in separate independent groups.
            3. So these independent groups are each DAGs.
      4. Now having these groups, we find topological ordering of each group's jobs. This gives us a safe order of execution within the group, which will guarantee that all the parent jobs will be executed
      first before any of its child gets executed.
      5. At this point, we should be happy with our job execution model. But here comes the tricky part: the execution order found by applying topological sort is not necessarily a good one. Since we are 
      executing the jobs serially and if one fails we do not proceed any further. It may be case that, the next job is not at all dependant on the failed job. So in this case, even though one more job could have
      been executed, we discard the rest of the execution queued jobs.
      6. One possible solution can be, whenever in a group job, if one job fails to execute. Then we can re-determine the topological sort order by swapping the position of the failing job with the next job. And then calculate
       the topological sort order one more time. This way, we will one by one move to the end of the list if the next jobs keeps on failing. But if one job is present which has no dependency then, this swapping - recalculationg
       top sort order will prevent from not executing that job. (This is a work in progress and the code is not committed.)


###### Assumptions:

Following assumptions has been taken while developing the application:

  1. A job will have one or more shell script attached to it. So job to script will be a one to many mapping. For now I am not going to make it as a many many to map due to time constraint.  
  
  2. Since the application does not have any knowledge on the internal processing of the scripts it run,
   the application will look for the stdout and stderr of the script to understand whether it finished with success or not.
   
  3. A job will be run at a fixed time in a day. So every time the application has been run, it will look for jobs which has not been run for today and then run those.
  
  4. A job is eligible to run if following two conditions are true:

     1. The job has no time scheduled or the job's scheduled time is due
     
     2. And the job has no dependency on other job(s) or all the jobs on which current job depends, are already completed.
     
  5. If a job fails then notification will be triggered. For simplicity it will be email for now.
  
  6. User will be able to start a failed job and also he may direct that all the dependent jobs on the failed job will be run even if their scheduled time for the day is over.
  
  7. Job will store the size of stdout data for the script in the database.
   
  8. A job will have a threshold value for data generated (lower and upper bound) and will notify if any of them are crossed.
  
  9. A job will also have a expected duration property to be input from user. Similar notification is expected if it crossed certain threshold.
  
  10. User will be able to manage jobs via console.
      
      
###### Test cases:
These are the cases which are available in the AppSpec file. Here the cases are noted down in more readable format. The cases are cumulative.
That is test case 3 will validate both test case 1 and 2. 

  1. One job with one script with no scheduled time.
   
  2. Two job with one script each with no scheduled time. The seconds job depends on the first job. 
  
  3. Add one more job which depends on NONE. But now make one job from test case 2 fail. This new job should not depends on the failure of previous job as it is
  not dependant on it.
  
  4. After correcting the job from test case 2, the failing job should be re run again. Since the parent job of the failing job has already been run successfully
  only the failed job should be run again.
  
  5. Now add a new job which depends on the failing job from test case 4. Now on the first run, the failing job will fail and thus the dependant new job will not be executed. In the second run, 
  after correcting the failing job, the failing job will run and then the dependant new job will also run.
  
  
###### Future improvements:

  1. Introduce threads (scala actors) to run independent set of jobs parallely.

  2. While identifying job execution order, handle following case more elegantly.
    Suppose Job-1 and Job-2 has no dependency and then Job-3 depends on both Job-1 and Job-2. Now say, the scheduling has been determined in this order:
    Job-1 -> Job-2 -> Job-3. If for some reason Job-1 fails, we should execute still execute **Job-2** as it is not dependant on any other job. But this case has not been handled in the implementation.
    
  3. Add support for setting job running time from UI.
  
  4. Add support for setting multiple scripts and watchers from the UI.
  
  