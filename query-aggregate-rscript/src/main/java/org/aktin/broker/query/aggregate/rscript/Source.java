package org.aktin.broker.query.aggregate.rscript;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class Source {
	@XmlAttribute
	String type;
	/**
	 * Timeout in seconds or milliseconds. Should match the pattern {@code [0-9]+m?s}
	 */
	@XmlAttribute
	String timeout;
	@XmlValue
	String value;
}
