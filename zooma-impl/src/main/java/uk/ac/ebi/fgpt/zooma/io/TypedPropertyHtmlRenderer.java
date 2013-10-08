package uk.ac.ebi.fgpt.zooma.io;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;

/**
 * An HtmlRenderer that is capable of generating summary views of ZOOMA typed property objects.
 *
 * @author Tony Burdett
 * @date 08/05/12
 */
@ServiceProvider
public class TypedPropertyHtmlRenderer implements HtmlRenderer<TypedProperty> {
    @Override public String getName() {
        return "Freebase ZOOMA Typed Property Renderer";
    }

    @Override public String getDescription() {
        return "Renders HTML summary views compatible with the freebase jQuery widget for ZOOMA properties";
    }

    @Override public boolean canRender(Object o) {
        return o instanceof TypedProperty;
    }

    @Override public Class<TypedProperty> getRenderingType() {
        return TypedProperty.class;
    }

    @Override public String renderHTML(TypedProperty typedProperty) {
        String propertyValue = typedProperty.getPropertyValue();
        String propertyType = typedProperty.getPropertyType();
        String propertyURI = typedProperty.getURI().toString();
        String propertyName = propertyValue + (propertyType != null ? " [" + propertyType + "]" : "");

        return "<div class=\"fbs-flyout-content\">" +
                "<img class=\"fbs-flyout-image-true\" id=\"fbs-topic-image\" " +
                "src=\"http://www.ebi.ac.uk/fgpt/zooma/images/typedProperty.png\">" +
                "<h1 class=\"fbs-flyout-image-true\" id=\"fbs-flyout-title\">" +
                propertyName +
                "</h1>" +
                "<strong>Element ID:</strong> " +
                propertyURI +
                "<p class=\"fbs-topic-article fbs-flyout-image-true\">" +
                "Property: '<span style=\"font-style: italic\">" + propertyValue + "</span>'<br>" +
                (propertyType != null ? " (of type '" + propertyType + "')" : "") +
                "</div>" +
                "<div class=\"fbs-attribution\">" +
                "<span class=\"fbs-flyout-types\">" +
                TypedProperty.PROPERTY_TYPE_NAME +
                "</span>";
    }
}
