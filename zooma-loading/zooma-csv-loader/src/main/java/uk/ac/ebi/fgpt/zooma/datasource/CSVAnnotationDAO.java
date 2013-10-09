package uk.ac.ebi.fgpt.zooma.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.exception.InvalidDataFormatException;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.text.SimpleDateFormat;
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
public class CSVAnnotationDAO extends Initializable implements AnnotationDAO {
    private final String datasourceName;
    private final String delimiter;

    private File annotationFile;
    private AnnotationFactory annotationFactory;

    private Map<String, Integer> columnIndexMap;
    private List<Annotation> annotations;

    private Logger log = LoggerFactory.getLogger(getClass());


    /**
     * Create a CSV annotation DAO with a default "tab" delimiter
     */
    private CSVAnnotationDAO(String datasourceName) {
        // use default delimiter of a tab
        this(datasourceName, "\t");
    }

    public CSVAnnotationDAO(String datasourceName, String delimiter) {
        this.datasourceName = datasourceName;
        this.delimiter = delimiter;

        this.columnIndexMap = Collections.synchronizedMap(new HashMap<String, Integer>());
        this.annotations = Collections.synchronizedList(new ArrayList<Annotation>());
    }

    protected Logger getLog() {
        return log;
    }

    public File getAnnotationFile() {
        return annotationFile;
    }

    public void setAnnotationFile(File annotationFile) {
        this.annotationFile = annotationFile;
    }

    public AnnotationFactory getAnnotationFactory() {
        return annotationFactory;
    }

    public void setAnnotationFactory(AnnotationFactory annotationFactory) {
        this.annotationFactory = annotationFactory;
    }

    @Override protected void doInitialization() throws Exception {
        getLog().debug("Parsing CSV file from " + getAnnotationFile().getAbsolutePath());

        // parse annotations file
        BufferedReader reader = new BufferedReader(new FileReader(getAnnotationFile()));
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
                URI bioentityURI = null;
                URI propertyURI = null;

                // optional ID attributes with null initializers
                String annotationID = null;
                String studyID = null;
                String bioentityID = null;
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
                if ((column = lookupColumn("BIOENTITY_ID")) != -1) {
                    bioentityID = annotationElements[column];
                }
                if ((column = lookupColumn("PROPERTY_ID")) != -1) {
                    propertyID = annotationElements[column];
                }
                if ((column = lookupColumn("ANNOTATOR")) != -1) {
                    annotator = annotationElements[column];
                }
                if ((column = lookupColumn("ANNOTATION_DATE")) != -1) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    annotationDate = formatter.parse(annotationElements[column]);
                }

                // now we've collected fields, generate annotation using annotation factory
                annotations.add(getAnnotationFactory().createAnnotation(annotationURI,
                                                                        annotationID,
                                                                        studyAcc,
                                                                        studyURI,
                                                                        studyID,
                                                                        bioentityName,
                                                                        bioentityURI,
                                                                        bioentityID,
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
        getLog().debug("Parsed file '" + getAnnotationFile().getAbsolutePath() + "' successfully, " +
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
        return annotations.size();
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a read-only DAO over a text file");
    }

    @Override public Collection<Annotation> read() {
        return Collections.unmodifiableCollection(annotations);
    }

    @Override public List<Annotation> read(int size, int start) {
        return Collections.unmodifiableList(annotations.subList(start, start + size));
    }

    @Override public Annotation read(URI uri) {
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
                                               "BIOENTITY_URI",
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
