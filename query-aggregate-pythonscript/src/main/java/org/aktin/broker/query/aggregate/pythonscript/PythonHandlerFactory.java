package org.aktin.broker.query.aggregate.pythonscript;

import org.aktin.broker.query.QueryHandlerFactory;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.function.Function;

// TODO add qualifier annotation

public class PythonHandlerFactory implements QueryHandlerFactory{
	private Charset exportCharset;
	private Path pythonExecPath;

	public PythonHandlerFactory(Path pythonExecPath) {
		this.exportCharset = StandardCharsets.UTF_8;
		this.pythonExecPath = pythonExecPath;
	}

	public Charset getExportCharset(){
		return exportCharset;
	}

	public Path getRExecutablePath() {
		return pythonExecPath;
	}

	@Override
	public String getElementName() {
		return PythonSource.XML_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return PythonSource.XML_NAMESPACE;
	}

	@Override
	public PythonHandler parse(Element element, Function<String,String> propertyLookup) {
		PythonSource q;
		try {
			JAXBContext c = JAXBContext.newInstance(PythonSource.class);
			q = (PythonSource)c.createUnmarshaller().unmarshal(element);
		} catch (JAXBException e) {
			throw new IllegalArgumentException("Unable to parse query XML", e);
		}
		return wrap(q, propertyLookup);
	}

	public PythonHandler wrap(PythonSource query, Function<String,String> propertyLookup){
		return new PythonHandler(this, query, propertyLookup);
	}
	@Override
	public String formatTimestamp(Instant timestamp) {
		return timestamp.toString();
	}
}
