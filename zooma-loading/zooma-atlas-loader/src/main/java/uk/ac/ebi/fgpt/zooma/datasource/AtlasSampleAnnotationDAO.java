package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * An Annotation DAO that is capable of reading sample annotations from the Gene Expression Atlas database.
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 03/04/2012
 */
public class AtlasSampleAnnotationDAO implements AnnotationDAO {
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
            "select distinct " +
                    "e.ACCESSION as STUDY, " +
                    "s.ACCESSION as BIOENTITY, " +
                    "p.NAME as PROPERTY_TYPE, " +
                    "pv.NAME as PROPERTY_VALUE, " +
                    "ot.ACCESSION as SEMANTIC_TAG " +
                    "from A2_EXPERIMENT e " +
                    "inner join A2_SAMPLE s on e.EXPERIMENTID = s.EXPERIMENTID " +
                    "inner join A2_SAMPLEPV spv on s.SAMPLEID = spv.SAMPLEID " +
                    "inner join A2_PROPERTYVALUE pv on spv.PROPERTYVALUEID = pv.PROPERTYVALUEID " +
                    "inner join A2_PROPERTY p on pv.PROPERTYID = p.PROPERTYID " +
                    "left join A2_SAMPLEPVONTOLOGY spvo on spv.SAMPLEPVID = spvo.SAMPLEPVID " +
                    "inner join A2_ONTOLOGYTERM ot on spvo.ONTOLOGYTERMID = ot.ONTOLOGYTERMID ";
    public static final String ANNOTATIONS_SELECT_COUNT =
            "select count(*) from (" + ANNOTATIONS_SELECT + ")";
    public static final String ORDERING =
            "order by e.ACCESSION, s.ACCESSION, p.NAME, pv.NAME asc";
    public static final String ANNOTATIONS_SELECT_ALL =
            ANNOTATIONS_SELECT + ORDERING;
    public static final String ANNOTATIONS_SELECT_LIMIT =
            "select STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, SEMANTIC_TAG from (" +
                    "select rownum R, STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, SEMANTIC_TAG from (" +
                    ANNOTATIONS_SELECT_ALL + ")) " +
                    "where R > ? and R <= ?";
    public static final String ANNOTATIONS_SELECT_BY_STUDY =
            ANNOTATIONS_SELECT + "where e.ACCESSION = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_BIOLOGICAL_ENTITY =
            ANNOTATIONS_SELECT + "where e.ACCESSION = ? and s.ACCESSION = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_PROPERTY =
            ANNOTATIONS_SELECT + "where pv.NAME = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_PROPERTY_AND_TYPE =
            ANNOTATIONS_SELECT + "where p.NAME = ? and pv.NAME = ?" + ORDERING;

    private final JDBCConventionBasedAnnotationMapper mapper;

    private JdbcTemplate jdbcTemplate;

    public AtlasSampleAnnotationDAO() {
        this(new AtlasAnnotationFactory(new AtlasSampleLoadingSession()));
    }

    public AtlasSampleAnnotationDAO(AtlasAnnotationFactory annotationFactory) {
        this(new AtlasAnnotationMapper(annotationFactory));
    }

    public AtlasSampleAnnotationDAO(AtlasAnnotationMapper annotationMapper) {
        this.mapper = annotationMapper;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override public String getDatasourceName() {
        return "gxa.samples";
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
        if (property instanceof TypedProperty) {
            return getJdbcTemplate().query(ANNOTATIONS_SELECT_BY_PROPERTY_AND_TYPE,
                                           mapper,
                                           ((TypedProperty) property).getPropertyType(),
                                           property.getPropertyValue());
        }
        else {
            return getJdbcTemplate().query(ANNOTATIONS_SELECT_BY_PROPERTY,
                                           mapper,
                                           property.getPropertyValue());
        }
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        throw new UnsupportedOperationException("Semantic tagging is not well defined in ArrayExpress, so retrieval " +
                                                        "of annotations using this method is not supported");
    }

    @Override public int count() {
        return getJdbcTemplate().queryForInt(ANNOTATIONS_SELECT_COUNT);
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
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

    @Override public void delete(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, deletions not supported");
    }
}
