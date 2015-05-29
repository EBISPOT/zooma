package uk.ac.ebi.fgpt.zooma.datasource;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.core.io.Resource;
import uk.ac.ebi.fgpt.zooma.exception.InvalidDataFormatException;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * An annotation DAO that is capable of extracting annotations from a text file.
 * <p>
 * Supplied text files should be in "CSV" format (comma-separated values), although the delimiter can be modified
 * (commas or tabs are commonly used).  The supplied file is parsed and cached in memory at startup, and DAO methods
 * read from this in-memory cache
 *
 * @author Tony Burdett
 * @date 23/10/12
 */
public class CSVAnnotationDAO extends RowBasedDataAnnotationMapper implements AnnotationDAO {
    private final String delimiter;

    private Resource csvResource;

    private Map<String, Integer> columnIndexMap;
    private List<Annotation> annotations;

    private static DateTimeFormatter dashedDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter slashDateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");

    /**
     * Create a CSV annotation DAO with a default "tab" delimiter
     */
    public CSVAnnotationDAO(AnnotationFactory annotationFactory, Resource csvResource) {
        // use default delimiter of a tab
        this(annotationFactory, csvResource, "\t");
    }

    public CSVAnnotationDAO(AnnotationFactory annotationFactory,
                            Resource csvResource,
                            String delimiter) {
        super(annotationFactory);
        this.delimiter = delimiter;
        this.csvResource = csvResource;

        this.columnIndexMap = Collections.synchronizedMap(new HashMap<String, Integer>());
        this.annotations = Collections.synchronizedList(new ArrayList<Annotation>());
    }

    @Override
    public synchronized boolean isReady() throws IllegalStateException {
        try {
            return super.isReady();
        }
        catch (IllegalStateException e) {
            throw new IllegalStateException("Initialization of " + getClass().getSimpleName() + " " +
                                                    "for datasource '" + getDatasourceName() + "' failed",
                                            e.getCause());
        }
    }

    @Override
    protected void doInitialization() throws Exception {
        getLog().debug("Parsing CSV file from input stream");

        // parse annotations file
        BufferedReader reader = new BufferedReader(new InputStreamReader(csvResource.getInputStream(), "UTF-8"));
        boolean readHeader = false;
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            // tokenize line
            String[] annotationElements = line.split(delimiter, -1);

            if (!readHeader) {
                // extract column indices from the header row
                readHeader = examineHeader(annotationElements);
            }
            else {
                // required attributes
                String studyAcc, bioentityName, propertyType, propertyValue;
                List<URI> semanticTags;

                // optional URI attributes with null initializers
                URI annotationURI = null;
                URI studyURI = null;
                URI studyType = null;
                URI bioentityURI = null;
                URI propertyURI = null;

                // optional ID attributes with null initializers
                String annotationID = null;
                String studyID = null;
                String bioentityID = null;
                String bioentityTypeName = null;
                URI bioentityTypeURI = null;
                String propertyID = null;
                String annotator = null;
                Date annotationDate = null;

                int column;

                // read elements and generate annotations
                List<String> missingColumns = new ArrayList<>();
                if ((column = lookupColumn("STUDY")) != -1) {
                    studyAcc = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                }
                else {
                    studyAcc = null;
                    missingColumns.add("STUDY");
                }
                if ((column = lookupColumn("BIOENTITY")) != -1) {
                    bioentityName = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                }
                else {
                    bioentityName = null;
                    missingColumns.add("BIOENTITY");
                }
                if ((column = lookupColumn("PROPERTY_TYPE")) != -1) {
                    propertyType = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                }
                else {
                    propertyType = null;
                    missingColumns.add("PROPERTY_TYPE");
                }
                if ((column = lookupColumn("PROPERTY_VALUE")) != -1) {
                    if (annotationElements[column].isEmpty()) {
                        propertyValue = null;
                        missingColumns.add("PROPERTY_VALUE");
                    }
                    else {
                        propertyValue = annotationElements[column];
                    }
                }
                else {
                    propertyValue = null;
                    missingColumns.add("PROPERTY_VALUE");
                }
                if ((column = lookupColumn("SEMANTIC_TAG")) != -1) {
                    String semanticTagsStr;
                    semanticTagsStr = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                    if (semanticTagsStr.contains("|")) {
                        semanticTags = new ArrayList<>();
                        for (String semanticTagStr : semanticTagsStr.split(Pattern.quote("|"))) {
                            semanticTags.add(convertSemanticTagToURI(semanticTagStr.trim()));
                        }
                    }
                    else {
                        semanticTags = Collections.singletonList(convertSemanticTagToURI(annotationElements[column]));
                    }
                }
                else {
                    semanticTags = Collections.singletonList(null);
                    missingColumns.add("SEMANTIC_TAG");
                }
                if (!missingColumns.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("The required element(s) ");
                    Iterator<String> missingColumnIt = missingColumns.iterator();
                    while (missingColumnIt.hasNext()) {
                        sb.append(missingColumnIt.next());
                        if (missingColumnIt.hasNext()) {
                            sb.append(", ");
                        }
                    }
                    sb.append(" are absent at line ").append(lineNumber).append(", result set cannot be mapped");
                    throw new InvalidDataFormatException(sb.toString());
                }

                // optional URI attributes
                if ((column = lookupColumn("ANNOTATION_URI")) != -1) {
                    annotationURI = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("STUDY_URI")) != -1) {
                    studyURI = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("BIOENTITY_URI")) != -1) {
                    bioentityURI = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("PROPERTY_URI")) != -1) {
                    propertyURI = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : URI.create(annotationElements[column]);
                }

                // optional ID attributes
                if ((column = lookupColumn("ANNOTATION_ID")) != -1) {
                    annotationID = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                }
                if ((column = lookupColumn("STUDY_ID")) != -1) {
                    studyID = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                }
                if ((column = lookupColumn("STUDY_TYPE")) != -1) {
                    studyType = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("BIOENTITY_ID")) != -1) {
                    bioentityID = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                }
                if ((column = lookupColumn("BIOENTITY_TYPE_URI")) != -1) {
                    bioentityTypeURI = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("BIOENTITY_TYPE_NAME")) != -1) {
                    bioentityTypeName = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                }
                if ((column = lookupColumn("PROPERTY_ID")) != -1) {
                    propertyID = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                }
                if ((column = lookupColumn("ANNOTATOR")) != -1) {
                    annotator = annotationElements.length <= column || annotationElements[column].isEmpty()
                            ? null
                            : annotationElements[column];
                }
                if ((column = lookupColumn("ANNOTATION_DATE")) != -1) {
                    if (annotationElements.length <= column || annotationElements[column].isEmpty()) {
                        annotationDate = null;
                    }
                    else {
                        String dateStr = annotationElements[column];
                        if (dateStr.contains("-")) {
                            annotationDate = dashedDateFormatter.parseDateTime(annotationElements[column]).toDate();
                        }
                        else if (dateStr.contains("/")) {
                            annotationDate = slashDateFormatter.parseDateTime(annotationElements[column]).toDate();
                        }
                        else {
                            getLog().error("Can't recognise format for date '" + dateStr + "' at line " + lineNumber);
                        }
                    }
                }

                // now we've collected fields, generate annotation using annotation factory
                for (URI semanticTag : semanticTags) {
                    annotations.add(createAnnotation(annotationURI,
                                                     annotationID,
                                                     studyAcc,
                                                     studyURI,
                                                     studyID,
                                                     studyType,
                                                     bioentityName,
                                                     bioentityURI,
                                                     bioentityID,
                                                     bioentityTypeName,
                                                     bioentityTypeURI,
                                                     propertyType,
                                                     propertyValue,
                                                     propertyURI,
                                                     propertyID,
                                                     semanticTag,
                                                     annotator,
                                                     annotationDate));
                }
            }
        }

        // now close the reader
        reader.close();
        getLog().debug("Parsed file successfully, " +
                               "read " + annotations.size() + " annotations");
    }

    @Override
    protected void doTermination() throws Exception {
        getLog().debug("Nothing to terminate");
    }

    @Override
    public String getDatasourceName() {
        return getAnnotationFactory().getDatasourceName();
    }

    @Override
    public Collection<Annotation> readByStudy(Study study) {
        Collection<Annotation> results = new HashSet<>();
        for (Annotation annotation : annotations) {
            for (BiologicalEntity be : annotation.getAnnotatedBiologicalEntities()) {
                if (be.getStudies().contains(study)) {
                    results.add(annotation);
                }
            }
        }
        return results;
    }

    @Override
    public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        Collection<Annotation> results = new HashSet<>();
        for (Annotation annotation : annotations) {
            if (annotation.getAnnotatedBiologicalEntities().contains(biologicalEntity)) {
                results.add(annotation);
            }
        }
        return results;
    }

    @Override
    public Collection<Annotation> readByProperty(Property property) {
        Collection<Annotation> results = new HashSet<>();
        for (Annotation annotation : annotations) {
            if (annotation.getAnnotatedProperty().matches(property)) {
                results.add(annotation);
            }
        }
        return results;
    }

    @Override
    public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        Collection<Annotation> results = new HashSet<>();
        for (Annotation annotation : annotations) {
            if (annotation.getSemanticTags().contains(semanticTagURI)) {
                results.add(annotation);
            }
        }
        return results;
    }

    @Override
    public int count() {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return annotations.size();
    }

    @Override
    public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a read-only DAO over a text file");
    }

    @Override
    public void create(Collection<Annotation> identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
    }

    @Override
    public Collection<Annotation> read() {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return Collections.unmodifiableCollection(annotations);
    }

    @Override
    public List<Annotation> read(int size, int start) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        if (start + size > annotations.size()) {
            return Collections.unmodifiableList(annotations.subList(start, annotations.size()));
        }
        else {
            return Collections.unmodifiableList(annotations.subList(start, start + size));
        }
    }

    @Override
    public Annotation read(URI uri) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        for (Annotation annotation : annotations) {
            if (annotation.getURI().equals(uri)) {
                return annotation;
            }
        }
        return null;
    }

    @Override
    public void update(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a read-only DAO over a text file");
    }

    @Override
    public void update(Collection<Annotation> object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a read-only DAO over a text file");
    }

    @Override
    public void delete(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a read-only DAO over a text file");
    }

    protected URI convertSemanticTagToURI(String semanticTag) {
        if (semanticTag == null) {
            return null;
        }
        else if (semanticTag.equals("")) {
            return null;
        }
        else {
            return URI.create(semanticTag.trim());
        }
    }

    private boolean examineHeader(String[] header) {
        // test if this is the header
        boolean isHeader = false;
        String[] allowedColumns = new String[]{"STUDY",
                "BIOENTITY",
                "PROPERTY_TYPE",
                "PROPERTY_VALUE",
                "SEMANTIC_TAG",
                "ANNOTATION_URI",
                "STUDY_URI",
                "STUDY_TYPE",
                "BIOENTITY_URI",
                "BIOENTITY_TYPE_NAME",
                "BIOENTITY_TYPE_URI",
                "PROPERTY_URI",
                "ANNOTATION_ID",
                "STUDY_ID",
                "BIOENTITY_ID",
                "PROPERTY_ID"};
        for (String allowedColumn : allowedColumns) {
            if (header[0].equalsIgnoreCase(allowedColumn)) {
                isHeader = true;
                break;
            }
        }

        if (isHeader) {
            if (columnIndexMap.isEmpty()) {
                for (int i = 0; i < header.length; i++) {
                    columnIndexMap.put(header[i], i);
                }
                return true;
            }
            return false;
        }
        else {
            return false;
        }
    }

    private int lookupColumn(String columnName) {
        if (columnIndexMap.containsKey(columnName)) {
            return columnIndexMap.get(columnName);
        }
        else {
            return -1;
        }
    }
}
