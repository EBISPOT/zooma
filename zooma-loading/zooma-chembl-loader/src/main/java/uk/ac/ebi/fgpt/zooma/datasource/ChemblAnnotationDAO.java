package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by dwelter on 28/05/14.
 */
public class ChemblAnnotationDAO implements AnnotationDAO {
    // ANNOTATION_URI [optional]
    // ANNOTATION_ID [optional]
    // STUDY
    // STUDY_URI [optional]
    // STUDY_ID [optional]
    // BIOENTITY
    // BIOENTITY_URI [optional]
    // BIOENTITY_ID [optional]
    // PROPERTY_TYPE
    // PROPERTY_VALUE
    // PROPERTY_URI [optional]
    // PROPERTY_ID [optional]
    // SEMANTIC_TAG


    public static final String ANNOTATIONS_SELECT =
            "select * from (" +
                    "select distinct " +
                    "d.PUBMED_ID as STUDY,  " +
                    "a.CHEMBL_ID as BIOENTITY, " +
                    "'CELL_LINE' as PROPERTY_TYPE, " +
                    "c.CELL_NAME as PROPERTY_VALUE, " +
                    "concat('http://www.ebi.ac.uk/efo/',c.EFO_ID) as SEMANTIC_TAG " +
                    "from CHEMBL_18.DOCS d " +
                    "join CHEMBL_18.ASSAYS a on d.DOC_ID=a.DOC_ID " +
                    "join CHEMBL_18.CELL_DICTIONARY c on a.CELL_ID=c.CELL_ID " +
                    "where c.EFO_ID is not null " +
                    "and a.CHEMBL_ID is not null and c.CELL_NAME is not null " +
                    "union " +
                    "select distinct " +
                    "d.PUBMED_ID as STUDY,  " +
                    "a.CHEMBL_ID as BIOENTITY, " +
                    "'CELL_LINE' as PROPERTY_TYPE, " +
                    "c.CELL_NAME as PROPERTY_VALUE, " +
                    "concat('http://purl.obolibrary.org/obo/',c.CLO_ID) as SEMANTIC_TAG " +
                    "from CHEMBL_18.DOCS d " +
                    "join CHEMBL_18.ASSAYS a on d.DOC_ID=a.DOC_ID " +
                    "join CHEMBL_18.CELL_DICTIONARY c on a.CELL_ID=c.CELL_ID " +
                    "where c.CLO_ID is not null " +
                    "and a.CHEMBL_ID is not null and c.CELL_NAME is not null) ";

    public static final String ANNOTATIONS_SELECT_COUNT =
            "select count(*) from (" + ANNOTATIONS_SELECT + ")";
    public static final String ORDERING =
            "order by STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE";
    public static final String ANNOTATIONS_SELECT_ALL =
            ANNOTATIONS_SELECT + ORDERING;
    public static final String ANNOTATIONS_SELECT_LIMIT =
            "select STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, SEMANTIC_TAG from (" +
                    "select rownum R, STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, SEMANTIC_TAG from (" +
                    ANNOTATIONS_SELECT_ALL + ")) " +
                    "where R > ? and R <= ?";
    public static final String ANNOTATIONS_SELECT_BY_STUDY =
            ANNOTATIONS_SELECT + "and d.PUBMED_ID = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_BIOLOGICAL_ENTITY =
            ANNOTATIONS_SELECT + "and d.PUBMED_ID = ? and a.CHEMBL_ID = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_PROPERTY =
            ANNOTATIONS_SELECT + "and c.CELL_NAME = ? " + ORDERING;

    private final JDBCConventionBasedAnnotationMapper mapper;

    private JdbcTemplate jdbcTemplate;

    public ChemblAnnotationDAO() {
        this(new ChemblAnnotationFactory(new ChemblLoadingSession()));
    }

    public ChemblAnnotationDAO(AnnotationFactory annotationFactory) {
        this(new JDBCConventionBasedAnnotationMapper(annotationFactory));
    }

    public ChemblAnnotationDAO(JDBCConventionBasedAnnotationMapper annotationMapper) {
        this.mapper = annotationMapper;
    }
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override public String getDatasourceName() {
        return "chembl";
    }

    @Override public Collection<Annotation> readByStudy(Study study) {
        return getJdbcTemplate().query(ANNOTATIONS_SELECT_BY_STUDY, mapper, study.getAccession());
    }

    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        Collection<Annotation> annotations = new HashSet<>();
        for (Study s : biologicalEntity.getStudies()) {
            annotations.addAll(getJdbcTemplate().query(ANNOTATIONS_SELECT_BY_BIOLOGICAL_ENTITY,
                    mapper,
                    s.getAccession(),
                    biologicalEntity.getName()));
        }
        return annotations;
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        return getJdbcTemplate().query(ANNOTATIONS_SELECT_BY_PROPERTY,
                mapper,
                property.getPropertyValue());

    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        throw new UnsupportedOperationException(
                "Semantic tagging is not well defined in the ChEMBL database, so retrieval " +
                        "of annotations using this method is not supported");
    }

    @Override public int count() {
        return getJdbcTemplate().queryForInt(ANNOTATIONS_SELECT_COUNT);
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
    }

    @Override public void create(Collection<Annotation> identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
    }

    @Override public Collection<Annotation> read() {
        return getJdbcTemplate().query(ANNOTATIONS_SELECT_ALL, mapper);
    }

    @Override public List<Annotation> read(int size, int start) {
        return getJdbcTemplate().query(ANNOTATIONS_SELECT_LIMIT, mapper, start, start + size);
    }

    @Override public Annotation read(URI uri) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support URI based lookups");
    }

    @Override public void update(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, updates not supported");
    }

    @Override public void update(Collection<Annotation> object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, updates not supported");
    }

    @Override public void delete(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, deletions not supported");
    }
}
