package uk.ac.ebi.fgpt.zooma.io;

import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.spi.ServiceProvider;

/**
 * An HtmlRenderer that is capable of generating summary views of ZOOMA property objects, as long as they are not typed
 * properties.
 *
 * @author Tony Burdett
 * @date 08/05/12
 */
@ServiceProvider
public class UntypedPropertyHtmlRenderer implements HtmlRenderer<Property> {
    @Override public String getName() {
        return "Freebase ZOOMA Non-typed Property Renderer";
    }

    @Override public String getDescription() {
        return "Renders HTML summary views compatible with the freebase jQuery widget for ZOOMA properties of unknown type";
    }

    @Override public boolean canRender(Object o) {
        return o instanceof Property && !(o instanceof TypedProperty);
    }

    @Override public Class<Property> getRenderingType() {
        return Property.class;
    }

    @Override public String renderHTML(Property untypedProperty) {
        String propertyValue = untypedProperty.getPropertyValue();
        String propertyURI = untypedProperty.getURI().toString();

        return "<div class=\"fbs-flyout-content\">" +
                "<img class=\"fbs-flyout-image-true\" id=\"fbs-topic-image\" " +
                "src=\"http://www.ebi.ac.uk/fgpt/zooma/images/property.png\">" +
                "<h1 class=\"fbs-flyout-image-true\" id=\"fbs-flyout-title\">" +
                propertyValue +
                "</h1>" +
                "<strong>Element ID:</strong> " +
                propertyURI +
                "<p class=\"fbs-topic-article fbs-flyout-image-true\">" +
                "Property: '<span style=\"font-style: italic\">" + propertyValue + "</span>'<br>" +
                " (of unknown type)</div>" +
                "<div class=\"fbs-attribution\">" +
                "<span class=\"fbs-flyout-types\">" +
                Property.PROPERTY_TYPE_NAME +
                "</span>";
    }
}
