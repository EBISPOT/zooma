package uk.ac.ebi.fgpt.zooma.io;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.Property;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * An HtmlRenderer that is capable of generating summary views of ZOOMA property types, which is a string.  The property
 * type string supplied is actually a URI (the property type freebase ID) which is simply the URI-encoded form of the
 * property type appended to the string "http://www.ebi.ac.uk/zooma/propertytype/"
 *
 * @author Tony Burdett
 * @date 08/05/12
 */
@ServiceProvider
public class PropertyTypeHtmlRenderer implements HtmlRenderer<String> {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override public String getName() {
        return "Freebase ZOOMA Property Type Renderer";
    }

    @Override public String getDescription() {
        return "Renders HTML summary views compatible with the freebase jQuery widget for ZOOMA property types";
    }

    @Override public boolean canRender(Object o) {
        return o instanceof String;
    }

    @Override public Class<String> getRenderingType() {
        return String.class;
    }

    @Override public String renderHTML(String propertyType) {
        try {
            String encoded = URLEncoder.encode(propertyType, "UTF-8");
            String propertyID =
                    "http://www.ebi.ac.uk".concat(Property.PROPERTYTYPE_TYPE_ID).concat("/").concat(encoded);
            return "<div class=\"fbs-flyout-content\">" +
                    "<img class=\"fbs-flyout-image-true\" id=\"fbs-topic-image\" " +
                    "src=\"http://www.ebi.ac.uk/fgpt/zooma/images/typedProperty.png\">" +
                    "<h1 class=\"fbs-flyout-image-true\" id=\"fbs-flyout-title\">" +
                    propertyType +
                    "</h1>" +
                    "<strong>Element ID:</strong> " +
                    propertyID +
                    "<p class=\"fbs-topic-article fbs-flyout-image-true\">" +
                    "Property Type: '<span style=\"font-style: italic\">" + propertyType + "</span>'<br>" +
                    "</div>" +
                    "<div class=\"fbs-attribution\">" +
                    "<span class=\"fbs-flyout-types\">" +
                    Property.PROPERTYTYPE_TYPE_NAME +
                    "</span>";
        }
        catch (UnsupportedEncodingException e) {
            getLog().error("Caught an UnsupportedEncodingException", e);
            throw new RuntimeException("Unexpected exception", e);
        }

    }
}
