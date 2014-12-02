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
 * An Annotation DAO that is capable of reading assay annotations from the ArrayExpress database, ready for curation in
 * the Expression Atlas.
 * <p/>
 * Note that this implementation returns an empty set when you attempt to recover all annotations, as this functionality
 * is not supported.  Instead, this class only returns results when you request annotations for specific studies
 *
 * @author Tony Burdett
 * @date 13/02/14
 */
public class ExpressionAtlasAssayAnnotationDAO implements AnnotationDAO {
    public static final String ANNOTATIONS_SELECT =
            "select STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE, null as SEMANTIC_TAG from (" +
                    "select x.STUDY, x.BIOENTITY, x.PROPERTY_TYPE, x.PROPERTY_VALUE from " +
                    "(select distinct s.ACC as STUDY, m.NAME as BIOENTITY, p.NAME as PROPERTY_TYPE, pv.NAME as PROPERTY_VALUE " +
                    " from STUDY s " +
                    " inner join NODE n on s.ID = n.STUDY_ID " +
                    " inner join NODEFACTORVALUEMAP nfv on n.ID = nfv.NODE_ID " +
                    " inner join MATERIAL m on n.MATERIAL_ID = m.ID " +
                    " inner join PROPERTY_VALUE pv on nfv.ID = pv.NODEFACTORVALUEMAP_ID " +
                    " inner join PROPERTY p on pv.PROPERTY_ID = p.ID " +
                    " where pv.OBJ_TYPE = 'FV' " +
                    " and pv.NAME is not null) x, " +
                    "(select STUDY, BIOENTITY, PROPERTY_TYPE, count(PROPERTY_TYPE) as FREQ from " +
                    " (select distinct s.ACC as STUDY, m.NAME as BIOENTITY, p.NAME as PROPERTY_TYPE, pv.NAME as PROPERTY_VALUE " +
                    "  from STUDY s " +
                    "  inner join NODE n on s.ID = n.STUDY_ID " +
                    "  inner join NODEFACTORVALUEMAP nfv on n.ID = nfv.NODE_ID " +
                    "  inner join MATERIAL m on n.MATERIAL_ID = m.ID " +
                    "  inner join PROPERTY_VALUE pv on nfv.ID = pv.NODEFACTORVALUEMAP_ID " +
                    "  inner join PROPERTY p on pv.PROPERTY_ID = p.ID " +
                    "  where pv.OBJ_TYPE = 'FV' " +
                    "  and pv.NAME is not null) " +
                    " group by STUDY, BIOENTITY, PROPERTY_TYPE) y " +
                    "where x.STUDY = y.STUDY " +
                    "and x.BIOENTITY = y.BIOENTITY " +
                    "and x.PROPERTY_TYPE = y.PROPERTY_TYPE " +
                    "and y.FREQ < 2) ";
    public static final String ORDERING =
            "order by STUDY, BIOENTITY, PROPERTY_TYPE, PROPERTY_VALUE asc";
    public static final String ANNOTATIONS_SELECT_BY_STUDY =
            ANNOTATIONS_SELECT + "where STUDY = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_BIOLOGICAL_ENTITY =
            ANNOTATIONS_SELECT + "where STUDY = ? and BIOENTITY = ? " + ORDERING;

    private final DefaultJdbcAnnotationMapper mapper;

    private JdbcTemplate jdbcTemplate;

    public ExpressionAtlasAssayAnnotationDAO(ExpressionAtlasAssayLoadingSession loadingSession) {
        mapper = new DefaultJdbcAnnotationMapper(new ExpressionAtlasAnnotationFactory(loadingSession));
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override public String getDatasourceName() {
        return "atlas.assays";
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
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support lookups that are not filtered by studies");
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        throw new UnsupportedOperationException("Semantic tagging is not well defined in ArrayExpress, so retrieval " +
                                                        "of annotations using this method is not supported");
    }

    @Override public int count() {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support count() - " +
                        "only queries that are filtered by studies are possible");
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
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support lookups that are not filtered by studies");
    }

    @Override public List<Annotation> read(int size, int start) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support lookups that are not filtered by studies");
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
