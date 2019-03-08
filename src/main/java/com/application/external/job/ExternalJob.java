
package com.application.external.job;

import java.util.Date;


public interface ExternalJob {

	String getJobId();

	int getPriority();

	Date getScheduledTime();

	boolean execute() throws Exception;

	void commit();

	void rollback();
}
