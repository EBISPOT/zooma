package uk.ac.ebi.spot.service;

import org.springframework.core.io.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import uk.ac.ebi.spot.datasource.AnnotationFactory;
import uk.ac.ebi.spot.exception.InvalidDataFormatException;
import uk.ac.ebi.spot.model.Annotation;

/**
 * A loader for CSV files
 * It will read the csv file and create an annotation for each line
 *
 * Uses an {@link uk.ac.ebi.spot.datasource.AnnotationFactory} to create the annotations
 *
 * Created by olgavrou on 08/08/2016.
 */
public class CSVLoader {

    private AnnotationFactory annotationFactory;

    private String delimiter;

    private Resource loadFrom;

    private Map<String, Integer> columnIndexMap = Collections.synchronizedMap(new HashMap<String, Integer>());
    private List<Annotation> annotations = Collections.synchronizedList(new ArrayList<Annotation>());

    private static DateTimeFormatter dashedDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter slashDateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");


    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public Resource getLoadFrom() {
        return loadFrom;
    }

    public void setLoadFrom(Resource loadFrom) {
        this.loadFrom = loadFrom;
    }

    public AnnotationFactory getAnnotationFactory() {
        return annotationFactory;
    }

    public void setAnnotationFactory(AnnotationFactory annotationFactory) {
        this.annotationFactory = annotationFactory;
    }

    /*
        Reads the csv file and creates the annotations
     */
    public List<Annotation> load() throws IOException {


        // parse annotations file
        BufferedReader reader = new BufferedReader(new InputStreamReader(loadFrom.getInputStream(), "UTF-8"));
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

                URI studyURI = null;
                URI bioentityURI = null;

                // optional attributes with null initializers
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

                // optional attributes
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
                            System.out.println("Can't recognise format for date '" + dateStr + "' at line " + lineNumber);
                        }
                    }
                }

                // now we've collected fields, generate annotation using annotation factory
                for (URI semanticTag : semanticTags) {
                    annotations.add(annotationFactory.createAnnotation(studyAcc,
                            studyURI,
                            bioentityName,
                            bioentityURI,
                            propertyType,
                            propertyValue,
                            semanticTag,
                            annotator,
                            annotationDate));
                }
            }
        }

        // now close the reader
        reader.close();

        return annotations;
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
                "STUDY_URI",
                "STUDY_TYPE",
                "BIOENTITY_URI",
                "BIOENTITY_TYPE_NAME",
                "BIOENTITY_TYPE_URI"};
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
