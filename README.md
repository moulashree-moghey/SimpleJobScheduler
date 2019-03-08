# SimpleJobSchedular

- This is a simple Quartz based java application which can be plugged-in to any other application requiring job-scheduling based task execution.
- As it's a prototype, only basic functionality is implemented and additional features can be added easily as and when needed.

Following is simple description of each class.

# Assumptions / limited implementations for prototype

- Few configuration values are hard-coded for simplicity, they can be moved to property/configuration files e.g.
	- DispayStatusJob run interval and priority
	- Quartz scheduler threadpool configuration, etc.
- Transaction management is not done explicitly
- All classes and methods are defined as public. In a real world scenario access needs to be restricted
- unit testcases are provided only for com.application.job.QuartzJob class

# com.application.external.job.ExternalJob

- ExternalJob is an interface exposed to outside world.
- Any client system which wants to use the job scheduler should create their tasks/jobs by implementing this interface.

# com.application.job.DisplayStatusJob

- DisplayStatusJob is additional internal job which gets triggered at predefined interval.
- It prints status of all the jobs which are either queued / running / successfully completed or failed after execution.
- As it's prototype
	- discarding old jobs is not implemented
	- Status display interval is hard-coded to 2 seconds and priority is set to -100.

# com.application.job.QuartzJob

- QuartzJob is responsible for actually triggering client's task/job
- It acts as a logical wrapper job to execute external jobs
- Based on success or failure of external job, it calls commit or rollback on external job
- Based on task execution and it's success/failure, it also updates task status in scheduler

# com.application.schedular.QuartzJobScheduler

- It is core implementation of job manager
- "submitExternalJob()" method is exposed to outside world to submit any task for scheduled / immediate execution
- "stop()" method exposed to outside world to stop scheduler from running for indefinite time

# Unit test cases

- Unit test cases are implemented using JUnit, Mockito and PowerMock frameworks
- Unit tests of com.application.job.QuartzJob verifies following cases
	- null job is submitted for execution by external application
	- when external job's execution starts, it's status is updated to RUNNING
	- external job is executed successfully, changes are committed and it's status is updated to SUCCESS
	- external job execution is failed, changes are rolled back and it's status is updated to FAILED
	- external job execution throws exception, changes are rolled back and it's status is updated to FAILED
