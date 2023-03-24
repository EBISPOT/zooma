package uk.ac.ebi.fgpt.zooma.io;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.io.IOException;
import java.net.URI;

/**
 * Serializes URIs in json objects by using the shortened form only - so "http://my.domain.com/foo" will become
 * something like "dom:foo".
 *
 * @author Tony Burdett
 * @date 17/09/12
 */
public class URIShortformJsonSerializer extends JsonSerializer<URI> {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override
    public void serialize(URI uri, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        // serialize the URI using only the short form
        String shortform = URIUtils.getShortform(uri);
        getLog().trace("Serializing '" + uri + "' to shortform '" + shortform + "'");
        jsonGenerator.writeString(shortform);
    }
}
