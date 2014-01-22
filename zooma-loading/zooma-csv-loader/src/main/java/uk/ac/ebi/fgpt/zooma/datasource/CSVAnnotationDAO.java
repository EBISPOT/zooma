package uk.ac.ebi.fgpt.zooma.datasource;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.exception.InvalidDataFormatException;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An annotation DAO that is capable of extracting annotations from a text file.
 * <p/>
 * Supplied text files should be in "CSV" format (comma-separated values), although the delimiter can be modified
 * (commas or tabs are commonly used).  The supplied file is parsed and cached in memory at startup, and DAO methods
 * read from this in-memory cache
 *
 * @author Tony Burdett
 * @date 23/10/12
 */
public class CSVAnnotationDAO extends RowBasedDataAnnotationMapper implements AnnotationDAO {
    private final String datasourceName;
    private final String delimiter;

    private InputStream inputStream;

    private Map<String, Integer> columnIndexMap;
    private List<Annotation> annotations;

    private Logger log = LoggerFactory.getLogger(getClass());

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public CSVAnnotationDAO(AnnotationFactory annotationFactory, File file, String datasourceName)
            throws FileNotFoundException {
        this(annotationFactory, new FileInputStream(file), datasourceName, "\t");
        getLog().debug("Parsing CSV file from file: " + file.getAbsolutePath());
    }

    public CSVAnnotationDAO(AnnotationFactory annotationFactory, URL url, String datasourceName) throws IOException {
        this(annotationFactory, url.openStream(), datasourceName, "\t");
        getLog().debug("Parsing CSV file from URL: " + url.getPath());
    }

    public CSVAnnotationDAO(AnnotationFactory annotationFactory, File file, String datasourceName, String delimiter)
            throws FileNotFoundException {
        this(annotationFactory, new FileInputStream(file), datasourceName, delimiter);
        getLog().debug("Parsing CSV file from file: " + file.getAbsolutePath());
    }

    public CSVAnnotationDAO(AnnotationFactory annotationFactory, URL url, String datasourceName, String delimiter)
            throws IOException {
        this(annotationFactory, url.openStream(), datasourceName, delimiter);
        getLog().debug("Parsing CSV file from URL: " + url.getPath());
    }

    /**
     * Create a CSV annotation DAO with a default "tab" delimiter
     */
    public CSVAnnotationDAO(AnnotationFactory annotationFactory, InputStream stream, String datasourceName) {
        // use default delimiter of a tab
        this(annotationFactory, stream, datasourceName, "\t");
    }

    public CSVAnnotationDAO(AnnotationFactory annotationFactory,
                            InputStream stream,
                            String datasourceName,
                            String delimiter) {
        super(annotationFactory);
        this.datasourceName = datasourceName;
        this.delimiter = delimiter;
        this.inputStream = stream;

        this.columnIndexMap = Collections.synchronizedMap(new HashMap<String, Integer>());
        this.annotations = Collections.synchronizedList(new ArrayList<Annotation>());
    }

    protected Logger getLog() {
        return log;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override protected void doInitialization() throws Exception {
        getLog().debug("Parsing CSV file from input stream");

        // parse annotations file
        BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream(), "UTF-8"));
        boolean readHeader = false;
        String line;
        while ((line = reader.readLine()) != null) {
            // tokenize line
            String[] annotationElements = line.split(delimiter, -1);

            if (!readHeader) {
                // extract column indices from the header row
                readHeader = examineHeader(annotationElements);
            }
            else {
                // required attributes
                String studyAcc, bioentityName, propertyType, propertyValue;
                URI semanticTag;

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
                    studyAcc = annotationElements[column];
                }
                else {
                    studyAcc = null;
                    missingColumns.add("STUDY");
                }
                if ((column = lookupColumn("BIOENTITY")) != -1) {
                    bioentityName = annotationElements[column];
                }
                else {
                    bioentityName = null;
                    missingColumns.add("BIOENTITY");
                }
                if ((column = lookupColumn("PROPERTY_TYPE")) != -1) {
                    propertyType = annotationElements[column];
                }
                else {
                    propertyType = null;
                    missingColumns.add("PROPERTY_TYPE");
                }
                if ((column = lookupColumn("PROPERTY_VALUE")) != -1) {
                    propertyValue = annotationElements[column];
                }
                else {
                    propertyValue = null;
                    missingColumns.add("PROPERTY_VALUE");
                }
                if ((column = lookupColumn("SEMANTIC_TAG")) != -1) {
                    semanticTag = convertSemanticTagToURI(annotationElements[column]);
                }
                else {
                    semanticTag = null;
                    missingColumns.add("SEMANTIC_TAG");
                }
                if (!missingColumns.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("The required column(s) ");
                    Iterator<String> missingColumnIt = missingColumns.iterator();
                    while (missingColumnIt.hasNext()) {
                        sb.append(missingColumnIt.next());
                        if (missingColumnIt.hasNext()) {
                            sb.append(", ");
                        }
                    }
                    sb.append(" are absent, result set cannot be mapped");
                    throw new InvalidDataFormatException(sb.toString());
                }

                // optional URI attributes
                if ((column = lookupColumn("ANNOTATION_URI")) != -1) {
                    annotationURI = URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("STUDY_URI")) != -1) {
                    studyURI = URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("BIOENTITY_URI")) != -1) {
                    bioentityURI = URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("PROPERTY_URI")) != -1) {
                    propertyURI = URI.create(annotationElements[column]);
                }

                // optional ID attributes
                if ((column = lookupColumn("ANNOTATION_ID")) != -1) {
                    annotationID = annotationElements[column];
                }
                if ((column = lookupColumn("STUDY_ID")) != -1) {
                    studyID = annotationElements[column];
                }
                if ((column = lookupColumn("STUDY_TYPE")) != -1) {
                    studyType = URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("BIOENTITY_ID")) != -1) {
                    bioentityID = annotationElements[column];
                }
                if ((column = lookupColumn("BIOENTITY_TYPE_URI")) != -1) {
                    bioentityTypeURI = URI.create(annotationElements[column]);
                }
                if ((column = lookupColumn("BIOENTITY_TYPE_NAME")) != -1) {
                    bioentityTypeName = annotationElements[column];
                }
                if ((column = lookupColumn("PROPERTY_ID")) != -1) {
                    propertyID = annotationElements[column];
                }
                if ((column = lookupColumn("ANNOTATOR")) != -1) {
                    annotator = annotationElements[column];
                }
                if ((column = lookupColumn("ANNOTATION_DATE")) != -1) {
                    annotationDate = formatter.parseDateTime(annotationElements[column]).toDate();
                }

                // now we've collected fields, generate annotation using annotation factory
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

        // now close the reader
        reader.close();
        getLog().debug("Parsed file successfully, " +
                               "read " + annotations.size() + " annotations");
    }

    @Override protected void doTermination() throws Exception {
        getLog().debug("Nothing to terminate");
    }

    @Override public String getDatasourceName() {
        return datasourceName;
    }

    @Override public Collection<Annotation> readByStudy(Study study) {
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

    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        Collection<Annotation> results = new HashSet<>();
        for (Annotation annotation : annotations) {
            if (annotation.getAnnotatedBiologicalEntities().contains(biologicalEntity)) {
                results.add(annotation);
            }
        }
        return results;
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        Collection<Annotation> results = new HashSet<>();
        for (Annotation annotation : annotations) {
            if (annotation.getAnnotatedProperty().matches(property)) {
                results.add(annotation);
            }
        }
        return results;
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        Collection<Annotation> results = new HashSet<>();
        for (Annotation annotation : annotations) {
            if (annotation.getSemanticTags().contains(semanticTagURI)) {
                results.add(annotation);
            }
        }
        return results;
    }

    @Override public int count() {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return annotations.size();
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a read-only DAO over a text file");
    }

    @Override public void create(Collection<Annotation> identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
    }

    @Override public Collection<Annotation> read() {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return Collections.unmodifiableCollection(annotations);
    }

    @Override public List<Annotation> read(int size, int start) {
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

    @Override public Annotation read(URI uri) {
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

    @Override public void update(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a read-only DAO over a text file");
    }

    @Override public void delete(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a read-only DAO over a text file");
    }

    protected URI convertSemanticTagToURI(String semanticTag) {
        if (semanticTag == null) {
            return null;
        }
        else {
            return URI.create(semanticTag);
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
