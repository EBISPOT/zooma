package uk.ac.ebi.fgpt.zooma.xml;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * A singleton wrapper around a SAXParserFactory to give us an easy method of obtaining a reference to a single,
 * reusable SAXParserFactory.
 *
 * @author Tony Burdett
 * @date 25/10/12
 */
public class SAXParserFactoryWrapper {
    private static SAXParserFactoryWrapper instance = new SAXParserFactoryWrapper();

    public static SAXParserFactoryWrapper getInstance() {
        return instance;
    }

    private final SAXParserFactory factory;

    private SAXParserFactoryWrapper() {
        this.factory = SAXParserFactory.newInstance();
        this.factory.setNamespaceAware(true);
    }

    public XMLReader createReader() throws SAXException, ParserConfigurationException {
        return factory.newSAXParser().getXMLReader();
    }
}
