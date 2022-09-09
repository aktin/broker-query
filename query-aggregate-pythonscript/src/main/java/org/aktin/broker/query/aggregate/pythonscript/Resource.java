package org.aktin.broker.query.aggregate.pythonscript;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class Resource {
    @XmlAttribute
    String type;
    @XmlAttribute
    String file;
    @XmlValue
    String value;
}
