# job-runner

Details to be added

###### Assumptions
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
      
      
###### Test Cases:
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
  
  
###### Improvements:

  1. Limit auto retry of failed job, but allow force run of a failed job
  