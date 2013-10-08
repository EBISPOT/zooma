package uk.ac.ebi.fgpt.zooma.io;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;

import java.net.URI;

/**
 * An HtmlRenderer that is capable of generating summary views of ZOOMA annotation objects
 *
 * @author Tony Burdett
 * @date 10/04/12
 */
@ServiceProvider
public class AnnotationHtmlRenderer implements HtmlRenderer<Annotation> {
    @Override public String getName() {
        return "Freebase ZOOMA Annotation Renderer";
    }

    @Override public String getDescription() {
        return "Renders HTML summary views compatible with the freebase jQuery widget for ZOOMA annotations";
    }

    @Override public boolean canRender(Object o) {
        return o instanceof Annotation;
    }

    @Override public Class<Annotation> getRenderingType() {
        return Annotation.class;
    }

    @Override public String renderHTML(Annotation annotation) {
        String propertyValue = annotation.getAnnotatedProperty().getPropertyValue();
        String propertyType = annotation.getAnnotatedProperty() instanceof TypedProperty
                ? ((TypedProperty) annotation.getAnnotatedProperty()).getPropertyType()
                : null;
        String annotationURI = annotation.getURI().toString();
        String annotationName = propertyValue + (propertyType != null ? " [" + propertyType + "]" : "");
        StringBuilder targetsHtml = new StringBuilder();
        for (URI semanticTag : annotation.getSemanticTags()) {
            targetsHtml.append("<a href=\"")
                    .append(semanticTag)
                    .append("\">")
                    .append(semanticTag)
                    .append("</a><br />");
        }

        return "<div class=\"fbs-flyout-content\">" +
                "<img class=\"fbs-flyout-image-true\" id=\"fbs-topic-image\" " +
                "src=\"http://www.ebi.ac.uk/fgpt/zooma/images/annotation.png\">" +
                "<h1 class=\"fbs-flyout-image-true\" id=\"fbs-flyout-title\">" +
                annotationName +
                "</h1>" +
                "<strong>Element ID:</strong> " +
                annotationURI +
                "<p class=\"fbs-topic-article fbs-flyout-image-true\">" +
                "Property: '<span style=\"font-style: italic\">" + propertyValue + "</span>'<br>" +
                (propertyType != null ? " (of type '" + propertyType + "')" : "") +
                "<p style=\"text-align: center\">annotates to</p>" +
                targetsHtml.toString() +
                "</p>" +
                "</div>" +
                "<div class=\"fbs-attribution\">" +
                "<span class=\"fbs-flyout-types\">" +
                Annotation.ANNOTATION_TYPE_NAME +
                "</span>";
    }
}
