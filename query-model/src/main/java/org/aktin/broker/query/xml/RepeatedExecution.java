package org.aktin.broker.query.xml;

import java.time.Period;

/**
 * Repeated execution schedule for a query.
 * 
 * The repeated execution does not contain a reference timestamp. Instead, the reference
 * timestamp is provided by the query request from the broker.
 * 
 * @author R.W.Majeed
 *
 */
public class RepeatedExecution extends QuerySchedule{

	/**
	 * Estimated repeating interval. Only positive periods allowed.
	 * This property will not cause any execution on the data warehouse side.
	 * It is solely for information purposes.
	 * <i>intervalHours</i> is used as a more fine-grained setting of intervals.
	 */
	public Period interval;
	public int intervalHours;
	public int id;
}
