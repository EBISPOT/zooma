package uk.ac.ebi.fgpt.zooma.io;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.util.LabelUtils;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * An HtmlRenderer that is capable of generating summary views of ZOOMA annotation summary objects
 *
 * @author Tony Burdett
 * @date 30/05/12
 */
@ServiceProvider
public class AnnotationSummaryHtmlRenderer implements HtmlRenderer<AnnotationSummary> {
    private Map<URI, String> uriImageMap;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public AnnotationSummaryHtmlRenderer() {
        // create image mapping
        uriImageMap = Collections.unmodifiableMap(setupImageMap());
    }

    @Override public String getName() {
        return "Freebase ZOOMA Annotation Summary Renderer";
    }

    @Override public String getDescription() {
        return "Renders HTML summary views compatible with the freebase jQuery widget for " +
                "unique combinations of ZOOMA properties and annotations to semantic tags";
    }

    @Override public boolean canRender(Object o) {
        return o instanceof AnnotationSummary;
    }

    @Override public Class<AnnotationSummary> getRenderingType() {
        return AnnotationSummary.class;
    }

    @Override public String renderHTML(AnnotationSummary annotationSummary) {
        String propertyValue = annotationSummary.getAnnotatedPropertyValue();
        String propertyType = annotationSummary.getAnnotatedPropertyType();

        Collection<URI> annotations = annotationSummary.getAnnotationURIs();
        Collection<URI> semanticTags = annotationSummary.getSemanticTags();

        float topScore = annotationSummary.getQualityScore();
        getLog().debug("Annotation summary top score: " + topScore);

        String quality;
        if (topScore > 40.0f) {
            quality = "are very high quality or highly curated";
        }
        else if (topScore > 38.0f) {
            quality = "are high quality";
        }
        else if (topScore > 35.0f) {
            quality = "are likely to be accurate, but may not be frequently used";
        }
        else {
            quality = "may not have been curated or may be context-specific";
        }

        Collection<String> annotationStrs = new HashSet<String>();
        for (URI uri : annotations) {
            annotationStrs.add(uri.toString());
        }
        StringBuilder annotationsHtml = new StringBuilder();

        int counter = 0;
        for (String s : annotationStrs) {
            // only show links to first 10 annotations in HTML view
            if (counter < 10) {
                annotationsHtml.append("<a href=\"")
                        .append(s)
                        .append("\">")
                        .append(s)
                        .append("</a><br />");
                counter++;
            }
            else {
                break;
            }
        }

        String imagePath = "http://www.ebi.ac.uk/fgpt/zooma/images/annotation_summary.png";
        StringBuilder annotatesTo = new StringBuilder();
        StringBuilder entitiesHtml = new StringBuilder();
        Iterator<URI> annIt = semanticTags.iterator();
        while (annIt.hasNext()) {
            counter++;
            URI uri = annIt.next();
            // override imagepath with more specific image if available
            imagePath = getImagePath(uri);
            // append shortform to brief description
            String shortname = URIUtils.getShortform(uri);
            String label = LabelUtils.getPrimaryLabel(uri);

            // append URI to entities html
            entitiesHtml.append(shortname);
            if (label != null && !label.equals("")) {
                entitiesHtml.append(" (");
                entitiesHtml.append(label);
                entitiesHtml.append(")");
            }

            if (annIt.hasNext()) {
                entitiesHtml.append(", ");
            }
        }

        String asID = annotationSummary.getID().length() > 20
                ? annotationSummary.getID().substring(0, 17) + "..."
                : annotationSummary.getID();

        return "<div class=\"fbs-flyout-content\">" +
                "<img class=\"fbs-flyout-image-true\" id=\"fbs-topic-image\" " +
                "src=\"" + imagePath + "\">" +
                "<h1 class=\"fbs-flyout-image-true\" id=\"fbs-flyout-title\">" +
                propertyValue + " (" + propertyType + ")" +
                "</h1>" +
                "<strong>Element ID:</strong> " +
                "<span title=\"" + annotationSummary.getID() + "\">" + asID + "</span><br/>" +
                "<h3><strong>Property Type:</strong> " + propertyType + "</h3>" +
                "<h3><strong>Property Value:</strong> " + propertyValue + "</h3>" +
                "<h3><strong>Semantic Tag:</strong> " + entitiesHtml + "</h3>" +
                "<p class=\"fbs-topic-article\" style=\"clear: none;\">" +
                "This pattern is used in " + annotationSummary.getAnnotationURIs().size() + " different annotations, " +
                "and includes annotations that " + quality + ".<br/>" +
                "</p>" +
                "</div>" +
                "<div class=\"fbs-attribution\">" +
                "<span class=\"fbs-flyout-types\">" +
                annotationSummary.getAnnotationSummaryTypeName() +
                "</span>";
    }

    private String getImagePath(URI semanticTag) {
        // we can separately render homo sapiens, subclasses of population, and subclasses of developmental stage
        String imagePath;
        if (uriImageMap.containsKey(semanticTag)) {
            imagePath = uriImageMap.get(semanticTag);
        }
        else {
            imagePath = "http://www.ebi.ac.uk/fgpt/zooma/images/annotation_summary.png";
        }
        getLog().debug("Summary shows annotation to " + semanticTag + ", which displays image '" + imagePath + "'");
        return imagePath;
    }

    private Map<URI, String> setupImageMap() {
        Map<URI, String> uriImageMap = new HashMap<>();

        // insert homo sapiens
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/NCBITaxon_9606"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_homo_sapiens.jpg");

        // insert developmental stages
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0000399"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001367"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002682"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001282"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001290"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001322"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002683"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001296"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001298"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001300"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001303"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002543"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001310"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002544"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002685"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001315"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0028002"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001272"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001367"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001323"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001355"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002948"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001382"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002684"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001992"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.org/obo/owl/CL#CL_0000365"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007057"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007112"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007047"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002682"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007016"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007010"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004450"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004451"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004452"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004453"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004454"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001273"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001274"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001276"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001277"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001278"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001279"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001280"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001281"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001283"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001282"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001284"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001285"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001286"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001287"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001288"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001289"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001290"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007045"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0025475"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007081"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002562"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002563"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002564"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002565"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002566"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002567"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002568"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002569"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002570"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002561"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002591"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001322"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007026"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007024"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002683"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001291"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001292"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002560"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001293"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001294"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001295"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001297"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001296"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002718"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001299"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001298"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002589"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001301"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001302"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001300"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002719"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002720"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001307"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001308"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001304"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001305"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001306"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001309"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001303"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007076"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007133"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007094"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007098"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007106"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007115"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007065"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007123"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007063"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007095"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007101"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007103"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007116"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007064"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007083"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007085"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007104"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007119"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007067"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007072"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007120"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007082"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007068"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004390"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002543"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001311"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001312"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001313"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001314"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001310"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002544"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007015"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002590"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007078"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007113"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002685"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007022"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001316"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001318"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001319"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001320"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001321"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001317"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001315"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0028002"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/PO_0007017"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004393"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004402"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002582"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004403"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004404"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004405"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004406"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004407"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002583"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004408"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004409"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004394"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004410"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002584"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002585"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004411"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002586"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004412"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002587"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004391"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002588"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004395"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004396"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004397"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004398"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004399"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004400"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004401"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001272"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0000295"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001367"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001323"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001355"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001372"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002721"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002948"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001382"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002684"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001992"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0002592"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");
        uriImageMap.put(URI.create("http://purl.org/obo/owl/CL#CL_0000365"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_developmental_stage.jpg");

        // insert populations
        uriImageMap.put(URI.create("http://purl.obolibrary.org/obo/OBI_0000181"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001799"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001271"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0000672"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004445"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003150"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003153"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003154"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003158"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001799"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003161"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0001271"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003167"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0000672"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004561"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003151"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003152"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003155"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003156"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003157"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0004445"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003159"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003160"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003168"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003169"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003162"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003163"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003164"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003165"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");
        uriImageMap.put(URI.create("http://www.ebi.ac.uk/efo/EFO_0003166"),
                        "http://www.ebi.ac.uk/fgpt/zooma/images/zooma_population.jpg");

        return uriImageMap;
    }
}
