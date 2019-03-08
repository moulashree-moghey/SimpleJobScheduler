
package com.application.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.application.external.job.ExternalJob;
import com.application.scheduler.QuartzJobScheduler;


public class QuartzJob implements Job {

	private static final Logger LOG = LogManager.getLogger(QuartzJob.class);


	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        String jobId = jobExecutionContext.getJobDetail().getJobDataMap().getString("jobId");

        ExternalJob externalJob = QuartzJobScheduler.getExternalJob(jobId);

        if (null == externalJob) {

			LOG.debug("No job to execute with jobId = [" + jobId + "]");
			return;
		}

        QuartzJobScheduler.setExternalJobStatus(jobId, QuartzJobScheduler.JobStatus.RUNNING);

		try {

			LOG.debug("Job with Id [" + externalJob.getJobId() + "] started");

			if (externalJob.execute()) {

				LOG.debug("Job with Id [" + externalJob.getJobId() + "] commit changes");

				externalJob.commit();

				QuartzJobScheduler.setExternalJobStatus(jobId, QuartzJobScheduler.JobStatus.SUCCESS);

				LOG.debug("Job with Id [" + externalJob.getJobId() + "] completed");

			} else {

				externalJob.rollback();

				LOG.debug("Job with Id [" + externalJob.getJobId() + "] changes rollbacked due to failure");

				QuartzJobScheduler.setExternalJobStatus(jobId, QuartzJobScheduler.JobStatus.FAILED);

				LOG.debug("Job with Id [" + externalJob.getJobId() + "] failed");
			}

		} catch (Exception e) {

			externalJob.rollback();

			LOG.debug("Job with Id [" + externalJob.getJobId() + "] changes rollbacked due to exception\n" + e.getMessage());

			QuartzJobScheduler.setExternalJobStatus(jobId, QuartzJobScheduler.JobStatus.FAILED);

			LOG.debug("Job with Id [" + externalJob.getJobId() + "] failed with exception");
		}
	}
}
