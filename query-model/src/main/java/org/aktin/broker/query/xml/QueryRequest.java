package org.aktin.broker.query.xml;

import javax.xml.bind.annotation.*;
import java.time.Instant;
import java.time.Period;

/**
 * Request execution of a query. The query is contained in this request type.
 * <p>
 * This execution request can occur multiple times for one and the same query.
 * In this case, the underlying query does not change, but multiple {@link QueryRequest}
 * instances are created containing the same query. This approach is commonly used for
 * repeating queries.
 * </p>
 *
 * @author R.W.Majeed
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryRequest {
    public static final String MEDIA_TYPE = "application/vnd.aktin.query.request+xml";
    /**
     * Unique id for the query request.
     * This id will always be unique and different
     * for multiple (e.g. recurring) requests for
     * the same query.
     * The id is given by the broker.
     */
    @XmlElement(required = true)
    int id;
    /**
     * Date reference for queries. A recurring
     * query may be requested once per month. The reference
     * date will then be used to determine the time frame
     * for the query. In other words, the reference date is used
     * to fill a placeholder in the query syntax.
     */
    @XmlElement(required = true)
    Instant reference;
    /**
     * Time stamp for the earliest execution / when the request is open.
     * If unspecified, the query can be executed at any time (usually
     * before the deadline)
     */
    @XmlElement(required = false)
    Instant scheduled;
    /**
     * Due date until which the query results have to be submitted.
     */
    @XmlElement(required = true)
    Instant deadline;
    /**
     * Query to execute
     */
    @XmlElement(required = true)
    Query query;
    @XmlElement(name = "signature", required = false)
    Signature[] signatures;

    /**
     * Default Constructor to unpack a received queries
     */
    public QueryRequest() {
    }

    /**
     * Argument Constructor to create queries
     */
    public QueryRequest(int id, Instant reference, Instant scheduled, Instant deadline, Query query) {
        this.id = id;
        this.reference = reference;
        this.scheduled = scheduled;
        this.deadline = deadline;
        this.query = query;
    }

    public int getId() {
        return id;
    }

    @XmlTransient
    public Integer getQueryId() {
        if (getQuery().schedule instanceof RepeatedExecution) {
            return ((RepeatedExecution) getQuery().schedule).id;
        } else {
            return null;
        }
    }

    @XmlTransient
    public Period getQueryInterval() {
        if (getQuery().schedule instanceof RepeatedExecution)
            return ((RepeatedExecution) getQuery().schedule).interval;
        else
            return null;

    }

    @XmlTransient
    public int getQueryIntervalHours() {
        if (getQuery().schedule instanceof RepeatedExecution)
            return ((RepeatedExecution) getQuery().schedule).intervalHours;
        else
            return 0;

    }

//	/**
//	 * Date when the request was canceled. This indicates abnormal
//	 * or unsuccessful termination of the request.
//	 * A request can not be closed and canceled at the same time.
//	 */
//	Instant canceled;

    /**
     * Timestamp for the earliest execution of the
     * query.
     *
     * @return timestamp
     */
    public Instant getScheduledTimestamp() {
        return scheduled;
    }

    /**
     * Reference timestamp to use for data extraction.
     * This will be combined with duration contained in the execution schedule
     * to obtain the start and end timestamps of the queried data.
     *
     * @return timestamp
     */
    public Instant getReferenceTimestamp() {
        return reference;
    }

    public Query getQuery() {
        return query;
    }
}
