package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A basic JDBC implementation of an annotation DAO.  This class will look for the classpath resource annotations.sql
 * when created.  This SQL file should contain the query by which annotations can be loaded from your JDBC datasource,
 * and must return fields obeying conventional naming criteria for ZOOMA annotations, as described below.
 * <p>
 * The result set must be formatted as a table with predefined columns.  Some of these are optional and will only be
 * used if supplied, others are required.  For example, if the given datasource supplies study, biological entity or
 * annotation URIs, these will be used in loading directly.  If URIs are absent but IDs are supplied, these will be used
 * to mint new URIs using a supplied URI minter.  If neither URIs or IDs are available, URIs will be minted using the
 * accession or name.
 * <p>
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
 * <p>
 * Optional fields may be excluded from the result set, but all others are required, even if values are null.
 *
 * @author Tony Burdett
 * @date 02/12/14
 */
public class DefaultJdbcAnnotationDAO implements AnnotationDAO {
    private final String ANNOTATIONS_SELECT_COUNT;
    private final String ANNOTATIONS_SELECT_ALL;
    private final String ANNOTATIONS_SELECT_LIMIT;
    private final String ANNOTATIONS_SELECT_BY_STUDY;
    private final String ANNOTATIONS_SELECT_BY_BIOLOGICAL_ENTITY;
    private final String ANNOTATIONS_SELECT_BY_PROPERTY;

    private final DefaultJdbcAnnotationMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    public DefaultJdbcAnnotationDAO(AnnotationFactory annotationFactory, JdbcTemplate jdbcTemplate) {
        this(new DefaultJdbcAnnotationMapper(annotationFactory), jdbcTemplate);
    }

    public DefaultJdbcAnnotationDAO(DefaultJdbcAnnotationMapper annotationMapper, JdbcTemplate jdbcTemplate) {
        this.mapper = annotationMapper;
        this.jdbcTemplate = jdbcTemplate;

        // now read sql file annotations.sql to get ANNOTATIONS_SELECT query
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                annotationMapper.getClass().getClassLoader().getResourceAsStream("annotations.sql")));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }

            String annotations_select = sb.toString().trim();
            String ordering = "order by STUDY, BIOENTITY, PROPERTY_VALUE asc";
            this.ANNOTATIONS_SELECT_COUNT = "select count(*) from (" + annotations_select + ")";
            this.ANNOTATIONS_SELECT_ALL =
                    "select * from (" + annotations_select + ") " + ordering + ";";
            this.ANNOTATIONS_SELECT_LIMIT =
                    "select STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, SEMANTIC_TAG from (" +
                            "select rownum R, STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, SEMANTIC_TAG from (" +
                            ANNOTATIONS_SELECT_ALL + ")) " +
                            "where R > ? and R <= ?";
            this.ANNOTATIONS_SELECT_BY_STUDY =
                    "select * from (" + annotations_select + ") where STUDY = ? " + ordering;
            this.ANNOTATIONS_SELECT_BY_BIOLOGICAL_ENTITY =
                    "select * from (" + annotations_select + ") where STUDY = ? and BIOENTITY = ? " + ordering;
            this.ANNOTATIONS_SELECT_BY_PROPERTY =
                    "select * from (" + annotations_select + ") where PROPERTY_VALUE = ? " + ordering;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load SQL to query for annotations", e);
        }
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public String getDatasourceName() {
        return mapper.getAnnotationFactory().getDatasourceName();
    }

    @Override
    public Collection<Annotation> readByStudy(Study study) {
        return getJdbcTemplate().query(ANNOTATIONS_SELECT_BY_STUDY, mapper, study.getAccession());
    }

    @Override
    public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        Collection<Annotation> annotations = new HashSet<>();
        for (Study s : biologicalEntity.getStudies()) {
            annotations.addAll(getJdbcTemplate().query(ANNOTATIONS_SELECT_BY_BIOLOGICAL_ENTITY,
                                                       mapper,
                                                       s.getAccession(),
                                                       biologicalEntity.getName()));
        }
        return annotations;
    }

    @Override
    public Collection<Annotation> readByProperty(Property property) {
        return getJdbcTemplate().query(ANNOTATIONS_SELECT_BY_PROPERTY,
                                       mapper,
                                       property.getPropertyValue());

    }

    @Override
    public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        throw new UnsupportedOperationException(
                "Semantic tagging is not well defined in the GWAS database, so retrieval " +
                        "of annotations using this method is not supported");
    }

    @Override
    public int count() {
        return getJdbcTemplate().queryForObject(ANNOTATIONS_SELECT_COUNT, Integer.class);
    }

    @Override
    public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
    }

    @Override
    public void create(Collection<Annotation> identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
    }

    @Override
    public Collection<Annotation> read() {
        return getJdbcTemplate().query(ANNOTATIONS_SELECT_ALL, mapper);
    }

    @Override
    public List<Annotation> read(int size, int start) {
        return getJdbcTemplate().query(ANNOTATIONS_SELECT_LIMIT, mapper, start, start + size);
    }

    @Override
    public Annotation read(URI uri) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support URI based lookups");
    }

    @Override
    public void update(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, updates not supported");
    }

    @Override
    public void update(Collection<Annotation> object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, updates not supported");
    }

    @Override
    public void delete(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, deletions not supported");
    }
}
