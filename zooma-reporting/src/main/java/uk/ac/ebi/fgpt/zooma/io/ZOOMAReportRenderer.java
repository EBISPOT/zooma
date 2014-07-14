package uk.ac.ebi.fgpt.zooma.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.OntologyLabelMapper;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A simple report renderer that writes an output file containing a list of ZOOMA optimal annotations for a list of
 * searched annotations.
 *
 * @author Tony Burdett
 * @date 03/09/12
 */
public class ZOOMAReportRenderer {
    private OntologyLabelMapper labelMapper;

    private OutputStream out;
    private OutputStream err;

    // time of execution start
    private Map<URI, String> labelCache;
    private Map<URI, Collection<String>> synonymCache;
    private String executionTime;
    private String workingDirectory;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZOOMAReportRenderer(OntologyLabelMapper labelMapper, String outputFile, String errorsFile)
            throws FileNotFoundException {
        this(labelMapper, new File(outputFile), new File(errorsFile));
    }

    public ZOOMAReportRenderer(OntologyLabelMapper labelMapper, File outputFile, File errorsFile)
            throws FileNotFoundException {
        this(labelMapper, new FileOutputStream(outputFile), new FileOutputStream(errorsFile));
    }

    public ZOOMAReportRenderer(OntologyLabelMapper labelMapper, OutputStream out, OutputStream err) {
        this.labelMapper = labelMapper;
        this.out = out;
        this.err = err;

        this.labelCache = new HashMap<>();
        this.synonymCache = new HashMap<>();
        this.executionTime = new SimpleDateFormat("HH:mm.ss, dd.MM.yy").format(new Date());
        this.workingDirectory = System.getProperty("user.dir");
    }


    public void renderAnnotations(List<Property> properties,
                                  Map<Property, List<String>> propertyContextMap,
                                  Map<Property, Set<Annotation>> propertyAnnotationMap,
                                  Map<Property, Boolean> propertySearchAchievedScoreMap) {
        // first, write the report header
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        writeReportHeader(writer);

        // if error report stream is different, write the error header too
        PrintWriter errorWriter;
        if (out == err) {
            errorWriter = writer;
        }
        else {
            errorWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(err)));
            writeReportHeader(errorWriter);
        }

        // now iterate over properties and write all annotations found for each one
        getLog().debug("Writing report of property mappings...");
        for (Property property : properties) {
            try {
                if (propertyAnnotationMap.containsKey(property)) {
                    Set<Annotation> annotations = propertyAnnotationMap.get(property);
                    getLog().debug("There are " + annotations.size() + " annotations for property '" + property + "'");

                    boolean achievedScore = propertySearchAchievedScoreMap.get(property);
                    getLog().debug("The search for property '" + property + "' " +
                                           (achievedScore ? "managed to" : "failed to") + " achieve the desired score");

                    // single best annotation, or several competing?
                    if (annotations.size() == 1 && achievedScore) {
                        // one good annotation, so render to the report with "auto" mapping
                        Annotation annotation = annotations.iterator().next();
                        // render one line per experiment, if known
                        if (propertyContextMap.containsKey(property)) {
                            for (String expt : propertyContextMap.get(property)) {
                                writeReportLine(writer,
                                                property,
                                                annotation.getAnnotatedProperty().getPropertyValue(),
                                                expt,
                                                annotation.getProvenance().getSource().getURI().toString(),
                                                annotation.getSemanticTags(),
                                                true);
                            }
                        }
                        else {
                            writeReportLine(writer,
                                            property,
                                            annotation.getAnnotatedProperty().getPropertyValue(),
                                            "[UNKNOWN EXPERIMENTS]",
                                            annotation.getProvenance().getSource().getURI().toString(),
                                            annotation.getSemanticTags(),
                                            true);
                        }
                    }
                    else {
                        // multiple annotations or 1 that isn't good enough, render to the report as "requires curation"
                        for (Annotation annotation : annotations) {
                            if (propertyContextMap.containsKey(property)) {
                                // render one line per experiment, if known
                                for (String expt : propertyContextMap.get(property)) {
                                    if (annotation.getSemanticTags() != null) {
                                        writeReportLine(writer,
                                                        property,
                                                        annotation.getAnnotatedProperty().getPropertyValue(),
                                                        expt,
                                                        annotation.getProvenance().getSource().getURI().toString(),
                                                        annotation.getSemanticTags(),
                                                        false);
                                    }
                                }
                            }
                            else {
                                if (annotation.getSemanticTags() != null) {
                                    writeReportLine(writer,
                                                    property,
                                                    annotation.getAnnotatedProperty().getPropertyValue(),
                                                    "[UNKNOWN EXPERIMENTS]",
                                                    annotation.getProvenance().getSource().getURI().toString(),
                                                    annotation.getSemanticTags(),
                                                    false);
                                }
                            }
                        }
                    }
                }
                else {
                    if (propertyContextMap.containsKey(property)) {
                        for (String expt : propertyContextMap.get(property)) {
                            writeUnmappedReportLine(errorWriter, property, expt);
                        }
                    }
                    else {
                        writeUnmappedReportLine(errorWriter, property, "[UNKNOWN EXPERIMENTS]");
                    }
                }
            }
            catch (Exception e) {
                getLog().error("Failed to write report line for property " + property + ".  Error was:", e);
                writeUnmappedReportLine(errorWriter, property, "[UNKNOWN EXPERIMENTS]");
            }
        }

        // flush the writer
        writer.flush();
        errorWriter.flush();
    }

    public void renderAnnotationSummaries(List<Property> properties,
                                          Map<Property, List<String>> propertyContextMap,
                                          Map<Property, Map<AnnotationSummary, Float>> propertySummariesMap) {
        // first, write the report header
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        writeEvaluationHeader(writer);

        // if error report stream is different, write the error header too
        PrintWriter errorWriter;
        if (out == err) {
            errorWriter = writer;
        }
        else {
            errorWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(err)));
            writeEvaluationHeader(errorWriter);
        }

        // now iterate over properties and write all annotations found for each one
        getLog().debug("Writing report of property mappings...");
        for (Property property : properties) {
            if (propertySummariesMap.containsKey(property)) {
                Map<AnnotationSummary, Float> summaries = propertySummariesMap.get(property);
                getLog().debug("There are " + summaries.keySet().size() + " summaries for property '" + property + "'");

                // render summaries for the given search along with scores
                final Map<AnnotationSummary, Float> summaryMap = propertySummariesMap.get(property);

                // sort summaries by score
                List<AnnotationSummary> summaryList = new ArrayList<>();
                summaryList.addAll(summaryMap.keySet());
                Collections.sort(summaryList, new Comparator<AnnotationSummary>() {
                    @Override public int compare(AnnotationSummary o1, AnnotationSummary o2) {
                        return summaryMap.get(o2).compareTo(summaryMap.get(o1));
                    }
                });

                // sublist if there are more than 20
                List<AnnotationSummary> reportList;
                if (summaryList.size() >= 20) {
                    reportList = summaryList.subList(0, 20);
                }
                else {
                    reportList = summaryList;
                }

                for (AnnotationSummary summary : reportList) {
                    if (propertyContextMap.containsKey(property)) {
                        writeEvaluationLine(writer,
                                            property,
                                            summary.getAnnotatedPropertyType(),
                                            summary.getAnnotatedPropertyValue(),
                                            summary.getSemanticTags(),
                                            summaryMap.get(summary));
                    }
                    else {
                        writeEvaluationLine(writer,
                                            property,
                                            summary.getAnnotatedPropertyType(),
                                            summary.getAnnotatedPropertyValue(),
                                            summary.getSemanticTags(),
                                            summaryMap.get(summary));
                    }
                }
            }
            else {
                if (propertyContextMap.containsKey(property)) {
                    writeUnmappedEvaluationLine(errorWriter, property);
                }
                else {
                    writeUnmappedEvaluationLine(errorWriter, property);
                }
            }
        }

        // flush the writer
        writer.flush();
        errorWriter.flush();
    }

    public void close() throws IOException {
        out.close();
        err.close();
    }

    protected void writeReportLine(PrintWriter writer,
                                   Property property,
                                   String matchedPropertyValue,
                                   String experiment,
                                   String source,
                                   Collection<URI> semanticTags,
                                   boolean automatic) {

        String type = automatic ? "Automatic" : "Requires curation";
        String propertyType =
                property instanceof TypedProperty ? ((TypedProperty) property).getPropertyType() : "[NO TYPE]";
        String propertyValue = property.getPropertyValue();

        if (getLog().isTraceEnabled()) {
            getLog().trace(
                    "Next annotation result to render:\n\t\t" +
                            "Searched: " + property + "\t" +
                            "Found annotation to: " + semanticTags.toString());
        }

        StringBuilder termsSB = new StringBuilder();
        StringBuilder labelsSB = new StringBuilder();
        StringBuilder synonymsSB = new StringBuilder();
        StringBuilder ontologiesSB = new StringBuilder();
        Iterator<URI> semanticTagIterator = semanticTags.iterator();
        while (semanticTagIterator.hasNext()) {
            URI semanticTag = semanticTagIterator.next();

            // use fragment as 'term'
            String term = URIUtils.extractFragment(semanticTag);
            // fetch the ontology label if possible
            String label = acquireLabel(semanticTag);
            // fetch the synonyms if possible
            Collection<String> synonyms = acquireSynonyms(semanticTag);
            // we consider the 'ontology' to be the URI minus the 'term'
            String ontology = semanticTag.toString().replace(term, "");

            termsSB.append(term);
            labelsSB.append(label != null && !label.isEmpty() ? label : matchedPropertyValue);
            Iterator<String> synonymsIterator = synonyms.iterator();
            while (synonymsIterator.hasNext()) {
                String synonym = synonymsIterator.next();
                synonymsSB.append(!synonym.isEmpty() ? synonym : "N/A");
                if (synonymsIterator.hasNext()) {
                    synonymsSB.append(", ");
                }
            }
            ontologiesSB.append(ontology);

            if (semanticTagIterator.hasNext()) {
                termsSB.append(", ");
                labelsSB.append(", ");
                synonymsSB.append("; ");
                ontologiesSB.append(", ");
            }
        }

        String terms = termsSB.toString();
        String labels = labelsSB.toString();
        String synonyms = synonymsSB.toString();
        String ontologies = ontologiesSB.toString();
        writer.println(propertyType + "\t" + propertyValue + "\t" + labels + "\t" +
                               synonyms + "\t" + type + "\t" + terms + "\t" +
                               ontologies + "\t" + source);
    }

    protected void writeUnmappedReportLine(PrintWriter writer,
                                           Property property,
                                           String experiment) {
        getLog().trace("There are no mappings for property '" + property + "'");

        String propertyType =
                property instanceof TypedProperty ? ((TypedProperty) property).getPropertyType() : "[NO TYPE]";
        String propertyValue = property.getPropertyValue();

        String type = "Did not map";
        String terms = "N/A";
        String labels = "N/A";
        String synonyms = "N/A";
        String ontologies = "N/A";
        String sources = "N/A";

        writer.println(propertyType + "\t" + propertyValue + "\t" + labels + "\t" +
                               synonyms + "\t" + type + "\t" + terms + "\t" +
                               ontologies + "\t" + sources);
    }

    protected void writeReportHeader(PrintWriter writer) {
        writer.println("Application Name:\tZOOMA (Automatic Ontology Mapper)");
        writer.println("Version:\t2.0");
        writer.println("Run at:\t" + executionTime);
        writer.println("Run from:\thttp://www.ebi.ac.uk/fgpt/zooma");
        writer.println();
        writer.println();
        writer.flush();

        writer.println(
                "PROPERTY TYPE\tPROPERTY VALUE\tONTOLOGY TERM LABEL(S)\tONTOLOGY TERM SYNONYM(S)\tMAPPING TYPE\tONTOLOGY TERM(S)\tONTOLOGY(S)\tSOURCE(S)");
    }

    protected void writeEvaluationLine(PrintWriter writer,
                                       Property searchedProperty,
                                       String matchedPropertyType,
                                       String matchedPropertyValue,
                                       Collection<URI> semanticTags,
                                       float score) {
        String searchedPropertyType =
                searchedProperty instanceof TypedProperty
                        ? ((TypedProperty) searchedProperty).getPropertyType()
                        : "[NO TYPE]";
        String searchedPropertyValue = searchedProperty.getPropertyValue();

        if (getLog().isTraceEnabled()) {
            getLog().trace(
                    "Next summary result to render:\n\t\t" +
                            "Searched: " + searchedProperty + "\t" +
                            "Found summary to: " + semanticTags.toString());
        }

        StringBuilder termsSB = new StringBuilder();
        StringBuilder labelsSB = new StringBuilder();
        StringBuilder synonymsSB = new StringBuilder();
        StringBuilder ontologiesSB = new StringBuilder();
        Iterator<URI> semanticTagIterator = semanticTags.iterator();
        while (semanticTagIterator.hasNext()) {
            URI semanticTag = semanticTagIterator.next();

            // use fragment as 'term'
            String term = URIUtils.extractFragment(semanticTag);
            // fetch the ontology label if possible
            String label = acquireLabel(semanticTag);
            // fetch the synonyms if possible
            Collection<String> synonyms = acquireSynonyms(semanticTag);
            // we consider the 'ontology' to be the URI minus the 'term'
            String ontology = semanticTag.toString().replace(term, "");

            termsSB.append(term);
            labelsSB.append(label != null && !label.isEmpty() ? label : "N/A");
            Iterator<String> synonymsIterator = synonyms.iterator();
            while (synonymsIterator.hasNext()) {
                String synonym = synonymsIterator.next();
                synonymsSB.append(!synonym.isEmpty() ? synonym : "N/A");
                if (synonymsIterator.hasNext()) {
                    synonymsSB.append(", ");
                }
            }
            ontologiesSB.append(ontology);

            if (semanticTagIterator.hasNext()) {
                termsSB.append(", ");
                labelsSB.append(", ");
                synonymsSB.append("; ");
                ontologiesSB.append(", ");
            }
        }

        String terms = termsSB.toString();
        String labels = labelsSB.toString();
        String synonyms = synonymsSB.toString();
        String ontologies = ontologiesSB.toString();

        writer.println(searchedPropertyType + "\t" + searchedPropertyValue + "\t" +
                               matchedPropertyType + "\t" + matchedPropertyValue + "\t" +
                               labels + "\t" + terms + "\t" + synonyms + "\t" + ontologies + "\t" + score);
    }

    protected void writeUnmappedEvaluationLine(PrintWriter writer,
                                               Property searchedProperty) {
        getLog().trace("There are no mappings for property '" + searchedProperty + "'");

        String searchedPropertyType =
                searchedProperty instanceof TypedProperty
                        ? ((TypedProperty) searchedProperty).getPropertyType()
                        : "[NO TYPE]";
        String searchedPropertyValue = searchedProperty.getPropertyValue();

        writer.println(searchedPropertyType + "\t" + searchedPropertyValue + "\t\t\t\t\t\t");
    }

    protected void writeEvaluationHeader(PrintWriter writer) {
        writer.println("Application Name:\tZOOMA (Automatic Ontology Mapper)");
        writer.println("Version:\t" + lookupVersionFromMavenProperties());
        writer.println("Run at:\t" + executionTime);
        writer.println("Run from:\t" + workingDirectory);
        try {
            writer.println("Execution host:\t" + InetAddress.getLocalHost().getCanonicalHostName());
        }
        catch (UnknownHostException e) {
            // we couldn't get the hostname, so just exclude this from the report
        }
        writer.println();
        writer.flush();

        writer.println(
                "SEARCHED PROPERTY TYPE\tSEARCHED PROPERTY VALUE\tMATCHED PROPERTY TYPE\tMATCHED PROPERTY VALUE\tONTOLOGY TERM LABEL(S)\tONTOLOGY TERM(S)\tONTOLOGY(S)\tSCORE");
    }

    /**
     * Retrieves the version number of the application, by locating the version property from the automatically
     * generated maven pom.properties file.
     *
     * @return the application version number
     */
    protected synchronized String lookupVersionFromMavenProperties() {
        String version;
        try {
            Properties properties = new Properties();
            InputStream in = getClass().getClassLoader().
                    getResourceAsStream("META-INF/maven/uk.ac.ebi.microarray/zooma/" +
                                                "pom.properties");
            properties.load(in);

            version = properties.getProperty("version");
        }
        catch (Exception e) {
            log.warn("Version number couldn't be discovered from pom.properties");
            version = "[Unknown]";
        }

        return version;
    }

    protected synchronized String acquireLabel(URI uri) {
        if (labelCache.containsKey(uri)) {
            return labelCache.get(uri);
        }
        else {
            String label;
            try {
                label = labelMapper.getLabel(uri);
            }
            catch (NullPointerException e) {
                getLog().warn("Label lookup for <" + uri + "> failed: " + e.getMessage());
                label = "N/A";
            }
            labelCache.put(uri, label);
            return label;
        }
    }

    protected synchronized Collection<String> acquireSynonyms(URI uri) {
        if (synonymCache.containsKey(uri)) {
            return synonymCache.get(uri);
        }
        else {
            Collection<String> synonyms;
            try {
                synonyms = labelMapper.getSynonyms(uri);
            }
            catch (NullPointerException e) {
                getLog().warn("Synonyms lookup for <" + uri + "> failed: " + e.getMessage());
                synonyms = Collections.singleton("N/A");
            }
            synonymCache.put(uri, synonyms);
            return synonyms;
        }
    }
}
