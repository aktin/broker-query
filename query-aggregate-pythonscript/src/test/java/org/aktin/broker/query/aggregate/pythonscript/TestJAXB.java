package org.aktin.broker.query.aggregate.pythonscript;


import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

public class TestJAXB {

	@Test
	public void unmarshallTestResource() throws IOException{
		PythonSource query;
		try( InputStream in = getClass().getResourceAsStream("/query-pythonscript.xml") ){
			query = JAXB.unmarshal(in, PythonSource.class);
		}
		assertNotNull(query);
		assertEquals(1, query.resource.size());
		assertEquals(1, query.result.size());
		assertEquals("python-generated-files.txt", query.resultList.file);
		assertEquals("text/plain", query.resource.get(0).type);
		assertEquals("module1.py", query.resource.get(0).file);

		assertEquals("text/tab-separated-values", query.result.get(0).type);
		assertEquals("counts.txt", query.result.get(0).file);

		assertNotNull(query.source);
		assertNotNull(query.source.value);
		assertEquals("application/python-script", query.source.type);
		assertEquals("30s", query.source.timeout);
		
		
	}
}
