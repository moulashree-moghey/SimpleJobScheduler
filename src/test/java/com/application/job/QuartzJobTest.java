
package com.application.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import com.application.external.job.ExternalJob;
import com.application.scheduler.QuartzJobScheduler;


@RunWith(PowerMockRunner.class)
@PrepareForTest(QuartzJobScheduler.class)
@PowerMockIgnore("javax.management.*")
public class QuartzJobTest {

	private JobDetail jobDetail = mock(JobDetail.class);
	private JobDataMap jobDataMap = mock(JobDataMap.class);
	private JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);

	private String jobId = "testJobId";

	private ExternalJob externalJob = mock(ExternalJob.class);
	private QuartzJob cut = new QuartzJob();


	@Before
	public void setUp() throws Exception {

		when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
		when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
		when(jobDataMap.getString(anyString())).thenReturn(jobId);

		PowerMockito.mockStatic(QuartzJobScheduler.class);
		PowerMockito.when(QuartzJobScheduler.getExternalJob(anyString())).thenReturn(externalJob);

		when(externalJob.getJobId()).thenReturn(jobId);
		when(externalJob.execute()).thenReturn(true);
	}


	@Test
	public void testExecute_NoExternalJobFound() {

		PowerMockito.when(QuartzJobScheduler.getExternalJob(anyString())).thenReturn(null);

		try {

			cut.execute(jobExecutionContext);

			PowerMockito.verifyStatic(QuartzJobScheduler.class, Mockito.never());
			QuartzJobScheduler.setExternalJobStatus(jobId, QuartzJobScheduler.JobStatus.RUNNING);

		} catch (Exception e) {

			fail("Failure due to exception");
		}
	}


	@Test
	public void testExecute_jobExecutionSuccess() {

		try {

			cut.execute(jobExecutionContext);

			PowerMockito.verifyStatic(QuartzJobScheduler.class, Mockito.times(1));
			QuartzJobScheduler.setExternalJobStatus(jobId, QuartzJobScheduler.JobStatus.RUNNING);

			verify(externalJob, Mockito.times(1)).commit();
			verify(externalJob, Mockito.never()).rollback();

			PowerMockito.verifyStatic(QuartzJobScheduler.class, Mockito.times(1));
			QuartzJobScheduler.setExternalJobStatus(jobId, QuartzJobScheduler.JobStatus.SUCCESS);

		} catch (Exception e) {

			fail("Failure due to exception");
		}
	}


	public void verifyFailedJob() throws Exception {

		cut.execute(jobExecutionContext);

		PowerMockito.verifyStatic(QuartzJobScheduler.class, Mockito.times(1));
		QuartzJobScheduler.setExternalJobStatus(jobId, QuartzJobScheduler.JobStatus.RUNNING);

		verify(externalJob, Mockito.never()).commit();
		verify(externalJob, Mockito.times(1)).rollback();

		PowerMockito.verifyStatic(QuartzJobScheduler.class, Mockito.times(1));
		QuartzJobScheduler.setExternalJobStatus(jobId, QuartzJobScheduler.JobStatus.FAILED);
	}


	@Test
	public void testExecute_jobExecutionFails() throws Exception {

		when(externalJob.execute()).thenReturn(false);
		verifyFailedJob();
	}


	@Test
	public void testExecute_jobExecutionThrowsException() throws Exception {

		when(externalJob.execute()).thenThrow(new Exception("test exception"));
		verifyFailedJob();
	}
}
