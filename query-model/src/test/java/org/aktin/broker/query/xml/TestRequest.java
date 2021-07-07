package org.aktin.broker.query.xml;

import javax.xml.bind.JAXB;

import org.aktin.broker.query.util.XIncludeUnmarshaller;
import org.junit.Assert;
import org.junit.Test;

import java.time.Period;

public class TestRequest {

	public static final QueryRequest getSingleRequest(int requestId){
		QueryRequest q = JAXB.unmarshal(XIncludeUnmarshaller.getXIncludeResource("/request.xml"), QueryRequest.class);
		q.id = requestId;
		//q.query.id = queryId;
		return q;
	}

	public static final QueryRequest getRepeatingRequest(int requestId, int queryId){
		QueryRequest q = JAXB.unmarshal(XIncludeUnmarshaller.getXIncludeResource("/request3.xml"), QueryRequest.class);
		q.id = requestId;
		((RepeatedExecution)q.query.schedule).id = queryId;
		return q;
	}

	@Test
	public void expectUnmarshalledDocumentComplete(){
		QueryRequest r = getSingleRequest(1);
		Assert.assertNotNull(r.deadline);
		Assert.assertNotNull(r.query);
	}

	@Test
	public void expectUnmarshalRepeatedExecution(){
		QueryRequest r = getRepeatingRequest(1,2);
		Assert.assertNotNull(r.deadline);
		Assert.assertNotNull(r.query);
		Assert.assertTrue(r.query.schedule instanceof RepeatedExecution);
		Assert.assertEquals(Period.parse("P1D"), r.getQueryInterval());
		Assert.assertEquals(6, r.getQueryIntervalHours());
		
	}

	@Test
	public void expectUnmarshalledDocumentComplete2(){
		QueryRequest q = JAXB.unmarshal(XIncludeUnmarshaller.getXIncludeResource("/request2.xml"), QueryRequest.class);
		Assert.assertNotNull(q.id);
		Assert.assertNotNull(q.query);
		Assert.assertNotNull(q.getScheduledTimestamp());
		Assert.assertNotNull(q.getReferenceTimestamp());
		Assert.assertNotNull(q.signatures);
		Assert.assertEquals(1, q.signatures.length);
		
	}

}
