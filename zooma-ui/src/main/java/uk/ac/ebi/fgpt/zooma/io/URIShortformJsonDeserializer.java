package uk.ac.ebi.fgpt.zooma.io;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.io.IOException;
import java.net.URI;

/**
 * Deserializes URIs in json objects by converting from the shortened form - so something like "dom:foo" will become
 * "http://my.domain.com/foo"
 *
 * @author Tony Burdett
 * @date 17/09/12
 */
public class URIShortformJsonDeserializer extends JsonDeserializer<URI> {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override public URI deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        // serialize the shortform to a URI
        URI uri = URIUtils.getURI(jsonParser.getText());
        getLog().debug("Deserializing shortform '" + jsonParser.getText() + "' to '" + uri + "'");
        return uri;
    }
}
