package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.fgpt.zooma.exception.InvalidDataFormatException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A Spring JDBC annotation mapper that can be used to generate ZOOMA annotations based on rows from an SQL result set.
 * For this class to map data correctly, you must supply the ID of the database this data is acquired from and an
 * evidence code to support these annotations.
 * <p/>
 * The result set must be formatted as a table with predefined columns.  Some of these are optional and will only be
 * used if supplied, others are required.  For example, if the given datasource supplies study, biological entity or
 * annotation URIs, these will be used in loading directly.  If URIs are absent but IDs are supplied, these will be used
 * to mint new URIs using a supplied URI minter.  If neither URIs or IDs are available, URIs will be minted using the
 * accession or name.
 * <p/>
 * The result set should be formatted with the following headings: <table> <thead> <tr> <th> ANNOTATION_URI</th> <th>
 * ANNOTATION_ID </th> <th> STUDY </th> <th> STUDY_URI </th> <th> STUDY_ID </th> <th> BIOENTITY </th> <th> BIOENTITY_URI
 * </th> <th> BIOENTITY_ID </th> <th> PROPERTY_TYPE </th> <th> PROPERTY_VALUE </th> <th> PROPERTY_URI </th> <th>
 * PROPERTY_ID </th> <th> SEMANTIC_TAG </th> <th> ANNOTATOR </th> <th> ANNOTATION_DATE </th></tr> </thead> <tbody> <tr>
 * <td> URI </td> <td> String </td> <td> String </td> <td> URI </td> <td> String </td> <td> String </td> <td> URI </td>
 * <td> String </td> <td> String </td> <td> String </td> <td> URI </td> <td> String </td> <td> URI </td> <td> String
 * </td> <td> Date </td> </td></td></tr> <tr> <td> The annotation URI, if available [Optional] </td> <td> The annotation
 * ID, if available [Optional] </td> <td> The study accession. </td> <td> The URI of this study, if available [Optional]
 * </td> <td> The ID of this study, if available [Optional] </td> <td> The bioentity name </td> <td> The URI of the
 * bioentity, if available [Optional] </td> <td> The ID of the bioentity, if available [Optional] </td> <td> The
 * property type </td> <td> The property value </td> <td> The URI of the property, if available [Optional] </td> <td>
 * The ID of the property, if available [Optional] </td> <td> The semantic tag. Required. If no database entry, set to
 * null [Nullable]. </td> <td> The person or algorithm that generated this annotation, if available [Optional] </td>
 * <td> The date this annotation was generated on.  Must be included if ANNOTATOR is included. [Optional] </td></tr>
 * </tbody> </table>
 * <p/>
 * Optional fields may be excluded from the result set, but all others are required, even if values are null.
 *
 * @author Tony Burdett
 * @date 26/09/12
 */
public class JDBCConventionBasedAnnotationMapper implements RowMapper<Annotation> {
    private final AnnotationFactory annotationFactory;
    private final Map<ResultSet, Map<String, Integer>> resultSetColumnIndexMap;


    public JDBCConventionBasedAnnotationMapper(AnnotationFactory annotationFactory) {
        this.annotationFactory = annotationFactory;
        this.resultSetColumnIndexMap = Collections.synchronizedMap(new HashMap<ResultSet, Map<String, Integer>>());
    }

    public AnnotationFactory getAnnotationFactory() {
        return annotationFactory;
    }

    public Annotation mapRow(ResultSet resultSet, int i) throws SQLException {
        // lazy init column mappings
        examineMetadata(resultSet);

        // columns in resultset...
        // ANNOTATION_URI [optional]
        // ANNOTATION_ID [optional]
        // STUDY
        // STUDY_URI [optional]
        // STUDY_ID [optional]
        // BIOENTITY
        // BIOENTITY_URI [optional]
        // BIOENTITY_ID [optional]
        // BIOENTITY_TYPE_NAME [optional]
        // BIOENTITY_TYPE_URI [optional]
        // PROPERTY_TYPE
        // PROPERTY_VALUE
        // PROPERTY_URI [optional]
        // PROPERTY_ID [optional]
        // SEMANTIC_TAG [nullable]
        // ANNOTATOR [nullable]
        // ANNOTATION_DATE [nullable]

        // required attributes
        String studyAcc = null;
        String bioentityName = null;
        String propertyType = null;
        String propertyValue = null;
        URI semanticTag = null;

        // optional URI attributes with null initializers
        URI annotationURI;
        URI studyURI;
        URI bioentityURI;
        URI propertyURI;

        // optional ID attributes with null initializers
        String annotationID;
        String studyID;
        String bioentityID;
        String bioentityTypeName;
        URI bioentityTypeURI;
        String propertyID;
        String annotator;
        Date annotationDate = null;

        int column;

        // required attributes
        List<String> missingColumns = new ArrayList<>();
        try {
            studyAcc = extractColumnValue(resultSet, "STUDY", true);
        }
        catch (NullPointerException e) {
            missingColumns.add("STUDY");
        }
        try {
            bioentityName = extractColumnValue(resultSet, "BIOENTITY", true);
        }
        catch (NullPointerException e) {
            missingColumns.add("BIOENTITY");
        }
        try {
            propertyType = extractColumnValue(resultSet, "PROPERTY_TYPE", true);
        }
        catch (NullPointerException e) {
            missingColumns.add("PROPERTY_TYPE");
        }
        try {
            propertyValue = extractColumnValue(resultSet, "PROPERTY_VALUE", true);
        }
        catch (NullPointerException e) {
            missingColumns.add("PROPERTY_VALUE");
        }
        try {
            semanticTag = convertSemanticTagToURI(extractColumnValue(resultSet, "SEMANTIC_TAG", true));
        }
        catch (NullPointerException e) {
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
            if (missingColumns.size() > 1) {
                sb.append(" are ");
            }
            else {
                sb.append(" is ");
            }
            sb.append("absent, result set cannot be mapped");
            throw new InvalidDataFormatException(sb.toString());
        }

        // optional URI attributes
        String uriStr;
        uriStr = extractColumnValue(resultSet, "ANNOTATION_URI");
        if (uriStr != null) {
            annotationURI = URI.create(uriStr);
        }
        else {
            annotationURI = null;
        }
        uriStr = extractColumnValue(resultSet, "STUDY_URI");
        if (uriStr != null) {
            studyURI = URI.create(uriStr);
        }
        else {
            studyURI = null;
        }
        uriStr = extractColumnValue(resultSet, "BIOENTITY_URI");
        if (uriStr != null) {
            bioentityURI = URI.create(uriStr);
        }
        else {
            bioentityURI = null;
        }
        uriStr = extractColumnValue(resultSet, "BIOENTITY_TYPE_URI");
        if (uriStr != null) {
            bioentityTypeURI = URI.create(uriStr);
        }
        else {
            bioentityTypeURI = null;
        }
        uriStr = extractColumnValue(resultSet, "BIOENTITY_TYPE_NAME");
        if (uriStr != null) {
            bioentityTypeName = uriStr;
        }
        else {
            bioentityTypeName = null;
        }
        uriStr = extractColumnValue(resultSet, "PROPERTY_URI");
        if (uriStr != null) {
            propertyURI = URI.create(uriStr);
        }
        else {
            propertyURI = null;
        }


        // optional ID attributes
        annotationID = extractColumnValue(resultSet, "ANNOTATION_ID");
        studyID = extractColumnValue(resultSet, "STUDY_ID");
        bioentityID = extractColumnValue(resultSet, "BIOENTITY_ID");
        propertyID = extractColumnValue(resultSet, "PROPERTY_ID");
        annotator = extractColumnValue(resultSet, "ANNOTATOR");
        if ((column = lookupColumn(resultSet, "ANNOTATION_DATE")) != -1) {
            Date val = resultSet.getDate(column);
            if (val != null) {
                annotationDate = val;
            }
        }

        // now we've collected fields, generate dependent objects using annotation factory
        return getAnnotationFactory().createAnnotation(annotationURI,
                                                       annotationID,
                                                       studyAcc,
                                                       studyURI,
                                                       studyID,
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
                                                       annotationDate);
    }

    protected URI convertSemanticTagToURI(String semanticTag) {
        if (semanticTag == null) {
            return null;
        }
        else {
            return URI.create(semanticTag);
        }
    }

    protected String extractColumnValue(ResultSet resultSet, String columnName) throws SQLException {
        return extractColumnValue(resultSet, columnName, false);
    }

    protected String extractColumnValue(ResultSet resultSet, String columnName, boolean isRequired)
            throws SQLException {
        int column;
        if ((column = lookupColumn(resultSet, columnName)) != -1) {
            String val = resultSet.getString(column);
            if (val != null && !val.isEmpty()) {
                return val;
            }
        }
        else {
            if (isRequired) {
                throw new NullPointerException("Absent required column '" + columnName + "'");
            }
        }
        return null;
    }

    private void examineMetadata(ResultSet resultSet) throws SQLException {
        Map<String, Integer> columnIndexMap;
        if (resultSetColumnIndexMap.containsKey(resultSet)) {
            columnIndexMap = resultSetColumnIndexMap.get(resultSet);
        }
        else {
            columnIndexMap = new HashMap<>();
        }

        if (columnIndexMap.isEmpty()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columnIndexMap.put(metaData.getColumnLabel(i), i);
            }
            resultSetColumnIndexMap.put(resultSet, columnIndexMap);
        }
    }

    private int lookupColumn(ResultSet resultSet, String columnName) {
        Map<String, Integer> columnIndexMap;
        if (resultSetColumnIndexMap.containsKey(resultSet)) {
            columnIndexMap = resultSetColumnIndexMap.get(resultSet);
            if (columnIndexMap.containsKey(columnName)) {
                return columnIndexMap.get(columnName);
            }
            else {
                return -1;
            }
        }
        else {
            throw new RuntimeException("Unexpected metadata lookup exception: " +
                                               "columns for metadata " + resultSet + " should be available, " +
                                               "the shit hit the fan bad somewhere!");
        }
    }
}
