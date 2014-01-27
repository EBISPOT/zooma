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
 * An Annotation DAO that is capable of reading data from the GWAS database.
 *
 * @author Dani Welter
 * @date 06/11/2012
 */
public class GwasAnnotationDAO implements AnnotationDAO {
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
                    "st.PMID as STUDY,  " +
                    "s.SNP as BIOENTITY, " +
                    "'GWAS_TRAIT' as PROPERTY_TYPE, " +
                    "t.DISEASETRAIT as PROPERTY_VALUE, " +
                    "e.EFOURI as SEMANTIC_TAG " +
                    "from GWASSNP s " +
                    "join GWASSNPXREF sx on s.ID=sx.SNPID " +
                    "join GWASSTUDIESSNP g on sx.GWASSTUDIESSNPID=g.ID " +
                    "join GWASSTUDIES st on g.GWASID=st.ID " +
                    "join GWASDISEASETRAITS t on st.DISEASEID=t.ID " +
                    "join GWASEFOSNPXREF ex on ex.GWASSTUDIESSNPID = g.ID " +
                    "join GWASEFOTRAITS e on e.ID = ex.TRAITID " +
                    "where g.ID is not null and s.SNP is not null " +
                    "and t.DISEASETRAIT is not null and g.PVALUEFLOAT is not null ";

    public static final String ANNOTATIONS_SELECT_COUNT =
            "select count(*) from (" + ANNOTATIONS_SELECT + ")";
    public static final String ORDERING =
            "order by st.PMID, s.SNP, t.DISEASETRAIT asc";
    public static final String ANNOTATIONS_SELECT_ALL =
            ANNOTATIONS_SELECT + ORDERING;
    public static final String ANNOTATIONS_SELECT_LIMIT =
            "select STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, SEMANTIC_TAG from (" +
                    "select rownum R, STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, SEMANTIC_TAG from (" +
                    ANNOTATIONS_SELECT_ALL + ")) " +
                    "where R > ? and R <= ?";
    public static final String ANNOTATIONS_SELECT_BY_STUDY =
            ANNOTATIONS_SELECT + "and st.PMID = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_BIOLOGICAL_ENTITY =
            ANNOTATIONS_SELECT + "and st.PMID = ? and s.SNP = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_PROPERTY =
            ANNOTATIONS_SELECT + "and t.DISEASETRAIT = ? " + ORDERING;

    private final JDBCConventionBasedAnnotationMapper mapper;

    private JdbcTemplate jdbcTemplate;

    public GwasAnnotationDAO() {
        this(new GwasAnnotationFactory(new GwasLoadingSession()));
    }

    public GwasAnnotationDAO(AnnotationFactory annotationFactory) {
        this(new GwasAnnotationMapper(annotationFactory));
    }

    public GwasAnnotationDAO(JDBCConventionBasedAnnotationMapper annotationMapper) {
        this.mapper = annotationMapper;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override public String getDatasourceName() {
        return "gwas";
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
                "Semantic tagging is not well defined in the GWAS database, so retrieval " +
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
