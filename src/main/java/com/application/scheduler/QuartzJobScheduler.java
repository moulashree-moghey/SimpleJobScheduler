
package com.application.scheduler;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.application.external.job.ExternalJob;
import com.application.job.DisplayStatusJob;
import com.application.job.QuartzJob;


public class QuartzJobScheduler {

	private static final Logger LOG = LogManager.getLogger(QuartzJobScheduler.class);

	public enum JobStatus { QUEUED, RUNNING, SUCCESS, FAILED }

	private static final int SHOW_STATUS_INTERVAL = 2;
	private static final int SHOW_STATUS_PRIORITY = -100;

	private static ConcurrentHashMap<String, ExternalJob> externalJobRepository;
	private static ConcurrentHashMap<String, JobStatus> externalJobStatus;

	private Scheduler scheduler;


	static {

		externalJobRepository = new ConcurrentHashMap<String, ExternalJob>();
		externalJobStatus = new ConcurrentHashMap<String, JobStatus>();
	}


	public static ExternalJob getExternalJob(String jobId) { return externalJobRepository.get(jobId); }

	public static String getAllExternalJobStatus() { return externalJobStatus.toString(); }

	public static void setExternalJobStatus(String jobId, JobStatus jobStatus) { externalJobStatus.put(jobId, jobStatus); }


	public QuartzJobScheduler() throws Exception {

		scheduler = new StdSchedulerFactory().getScheduler();

		JobDetail jobDetail = JobBuilder.newJob(DisplayStatusJob.class)
								.withIdentity("DispayStatusJob")
								.build();

		Trigger trigger = TriggerBuilder.newTrigger()
							.withIdentity("DispayStatusJob")
							.withPriority(SHOW_STATUS_PRIORITY)
							.startNow()
							.withSchedule(SimpleScheduleBuilder.simpleSchedule()
											.withIntervalInSeconds(SHOW_STATUS_INTERVAL)
											.repeatForever())
							.build();

		scheduler.scheduleJob(jobDetail, trigger);

		scheduler.start();

		LOG.debug("QuartzJobSchedular started");
	}


	public void stop() throws Exception {

		scheduler.shutdown();

		LOG.debug("QuartzJobSchedular stopped");
	}


	public boolean submitExternalJob(ExternalJob externalJob) throws Exception {

		if (null == externalJob) {

			LOG.debug("Can't schedule non-existing external job");
			return true;
		}

		LOG.debug("Received external job with jobId = [" + externalJob.getJobId() + "]");

		JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class)
								.withIdentity(externalJob.getJobId())
								.usingJobData("jobId", externalJob.getJobId())
								.build();

		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
											.withIdentity(externalJob.getJobId())
											.withPriority(externalJob.getPriority());

		Date scheduledTime = externalJob.getScheduledTime();

        triggerBuilder = (null == scheduledTime) ? triggerBuilder.startNow() : triggerBuilder.startAt(scheduledTime);

        externalJobRepository.put(externalJob.getJobId(), externalJob);
        externalJobStatus.put(externalJob.getJobId(), JobStatus.QUEUED);

        scheduler.scheduleJob(jobDetail, triggerBuilder.build());

        LOG.debug("External job with jobId = [" + externalJob.getJobId() + "] is QUEUED to run " + ((null == scheduledTime) ? "immediately" : "at [" + scheduledTime + "]"));
        return true;
	}
}
