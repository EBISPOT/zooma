package uk.ac.ebi.fgpt.zooma.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * A simple proxy class that wraps an instance of {@link org.xml.sax.XMLReader}.
 * <p/>
 * This class exists to optimize Sesame's XML parsing strategy by utilizing a singleton {@link
 * javax.xml.parsers.SAXParserFactory} instead of generating a new factory per-invocation.
 *
 * @author Tony Burdett
 * @date 25/10/12
 */
public class ZoomaXMLReaderProxy implements XMLReader {
    private final XMLReader xmlReader;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZoomaXMLReaderProxy() throws InstantiationException {
        try {
            getLog().debug("Using " + getClass().getSimpleName() + " to create XMLReader");
            this.xmlReader = SAXParserFactoryWrapper.getInstance().createReader();
        }
        catch (SAXException | ParserConfigurationException e) {
            getLog().error("XML Reader creation failed", e);
            throw new InstantiationException(
                    "Unable to use singleton SAXParserFactoryWrapper to instantiate XMLReader " +
                            "(" + e.getMessage() + ")");
        }
    }

    @Override public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return xmlReader.getFeature(name);
    }

    @Override public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        xmlReader.setFeature(name, value);
    }

    @Override public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return xmlReader.getProperty(name);
    }

    @Override public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        xmlReader.setProperty(name, value);
    }

    @Override public void setEntityResolver(EntityResolver resolver) {
        xmlReader.setEntityResolver(resolver);
    }

    @Override public EntityResolver getEntityResolver() {
        return xmlReader.getEntityResolver();
    }

    @Override public void setDTDHandler(DTDHandler handler) {
        xmlReader.setDTDHandler(handler);
    }

    @Override public DTDHandler getDTDHandler() {
        return xmlReader.getDTDHandler();
    }

    @Override public void setContentHandler(ContentHandler handler) {
        xmlReader.setContentHandler(handler);
    }

    @Override public ContentHandler getContentHandler() {
        return xmlReader.getContentHandler();
    }

    @Override public void setErrorHandler(ErrorHandler handler) {
        xmlReader.setErrorHandler(handler);
    }

    @Override public ErrorHandler getErrorHandler() {
        return xmlReader.getErrorHandler();
    }

    @Override public void parse(InputSource input) throws IOException, SAXException {
        xmlReader.parse(input);
    }

    @Override public void parse(String systemId) throws IOException, SAXException {
        xmlReader.parse(systemId);
    }
}
