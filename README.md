# job-runner

Details to be added

###### Assumptions
Following assumptions has been taken while developing the application:

  1. A job will have one shell script attached to it.  
  
  2. Since the application does not have any knowledge on the internal processing of the scripts it run,
   the application will look for the stdout and stderr of the script to understand whether it finished with success or not.
   
  3. A job will be run at a fixed time in a day. So everytime the application has been run, it will look for jobs which has not been run for today and then run those.
  
  4. A job is eligible to run if following two conditions are true:

     1. The job has no time scheduled or the job's scheduled time is due
     
     2. And the job has no dependency on other job(s) or all the jobs on which current job depends, are already completed.
     
  5. If a job fails then notification will be triggered. For simplicity it will be email for now.
  
  6. User will be able to start a failed job and also he may direct that all the dependent jobs on the failed job will be run even if their scheduled time for the day is over.
  
  7. Job will store the size of stdout data for the script in the database.
   
  8. A job will have a threshold value for data generated (lower and upper bound) and will notify if any of them are crossed.
  
  9. A job will also have a average time limit to be input from user. Similar notification is expected if it crossed certain threshold.
  
  10. User will be able to manage jobs via console.
      