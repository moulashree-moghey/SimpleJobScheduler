
package com.application.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.application.scheduler.QuartzJobScheduler;


public class DisplayStatusJob implements Job {

	private static final Logger LOG = LogManager.getLogger(DisplayStatusJob.class);


	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		LOG.debug("************ Job Statuses = " + QuartzJobScheduler.getAllExternalJobStatus());
	}
}
