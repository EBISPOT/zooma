package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.fgpt.zooma.Namespaces;
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
 * An annotation DAO that is capable of reading annotations from an instance of the OMIA MySQL database.
 *
 * @author Tony Burdett
 * @date 26/07/13
 */
public class OmiaAnnotationDAO implements AnnotationDAO {
    // ANNOTATION_URI [optional]
    // ANNOTATION_ID [optional]
    // STUDY
    // STUDY_URI [optional]
    // STUDY_ID [optional]
    // BIOENTITY
    // BIOENTITY_URI [optional]
    // BIOENTITY_ID [optional]
    // BIOENTITY_TYPE_NAME [optional]
    // PROPERTY_TYPE
    // PROPERTY_VALUE
    // PROPERTY_URI [optional]
    // PROPERTY_ID [optional]
    // SEMANTIC_TAG
    public static final String ANNOTATIONS_SELECT =
            "select distinct STUDY, BIOENTITY, BIOENTITY_ID, BIOENTITY_URI, BIOENTITY_TYPE_NAME, PROPERTY_TYPE, PROPERTY_VALUE, PROPERTY_ID, SEMANTIC_TAG, ANNOTATION_DATE " +
                    "from ( " +
                    "select a.pubmed_id as STUDY, g.symbol as BIOENTITY, CONCAT('gene', g.gene_id) as BIOENTITY_ID, null as BIOENTITY_URI, 'gene' as BIOENTITY_TYPE_NAME, 'phenotype' as PROPERTY_TYPE, og.group_name as PROPERTY_VALUE, CONCAT('OMIA', og.omia_id) as PROPERTY_ID, CONCAT('" + Namespaces.OMIA.getURI().toString() + "/OMIA', p.omia_id, '/', p.gb_species_id) as SEMANTIC_TAG, p.date_modified as ANNOTATION_DATE " +
                    "from Phene p " +
                    "left join OMIA_Group og on p.omia_id = og.omia_id " +
                    "left join Article_Phene ap on p.phene_id = ap.phene_id " +
                    "left join Articles a on ap.article_id = a.article_id " +
                    "left join Phene_Gene pg on p.phene_id = pg.phene_id " +
                    "left join Genes_gb g on pg.gene_id = g.gene_id " +
                    "where g.gene_id is not null " +
                    "union " +
                    "select a.pubmed_id as STUDY, g.symbol as BIOENTITY, CONCAT('gene', g.gene_id) as BIOENTITY_ID, null as BIOENTITY_URI,  'gene' as BIOENTITY_TYPE_NAME, 'species' as PROPERTY_TYPE, s.sci_name as PROPERTY_VALUE, null as PROPERTY_ID, CONCAT('" + Namespaces.NCBITAXON.getURI().toString() + "NCBITaxon_', s.gb_species_id) as SEMANTIC_TAG, p.date_modified as ANNOTATION_DATE " +
                    "from Phene p " +
                    "left join Species_gb s on p.gb_species_id = s.gb_species_id " +
                    "left join Article_Phene ap on p.phene_id = ap.phene_id " +
                    "left join Articles a on ap.article_id = a.article_id " +
                    "left join Phene_Gene pg on p.phene_id = pg.phene_id " +
                    "left join Genes_gb g on pg.gene_id = g.gene_id " +
                    "where g.gene_id is not null " +
                    "union " +
                    "select a.pubmed_id as STUDY, p.symbol as BIOENTITY, null as BIOENTITY_ID, CONCAT('" + Namespaces.ZOOMA_RESOURCE.getURI().toString() + "omia/phene_', p.phene_id) as BIOENTITY_URI,  'phenotype' as BIOENTITY_TYPE_NAME, 'phenotype' as PROPERTY_TYPE, og.group_name as PROPERTY_VALUE, CONCAT('OMIA', og.omia_id) as PROPERTY_ID, CONCAT('" + Namespaces.OMIA.getURI().toString() + "/OMIA', p.omia_id, '/', p.gb_species_id) as SEMANTIC_TAG, p.date_modified as ANNOTATION_DATE " +
                    "from Phene p " +
                    "left join OMIA_Group og on p.omia_id = og.omia_id " +
                    "left join Article_Phene ap on p.phene_id = ap.phene_id " +
                    "left join Articles a on ap.article_id = a.article_id " +
                    "left join Phene_Gene pg on p.phene_id = pg.phene_id " +
                    "left join Genes_gb g on pg.gene_id = g.gene_id " +
                    "where g.gene_id is null " +
                    "union " +
                    "select a.pubmed_id as STUDY, p.symbol as BIOENTITY, null as BIOENTITY_ID, CONCAT('" + Namespaces.ZOOMA_RESOURCE.getURI().toString() + "omia/phene_', p.phene_id) as BIOENTITY_URI, 'phenotype' as BIOENTITY_TYPE_NAME, 'species' as PROPERTY_TYPE, s.sci_name as PROPERTY_VALUE, null as PROPERTY_ID, CONCAT('" + Namespaces.NCBITAXON.getURI().toString() + "NCBITaxon_', s.gb_species_id) as SEMANTIC_TAG, p.date_modified as ANNOTATION_DATE " +
                    "from Phene p " +
                    "left join Species_gb s on p.gb_species_id = s.gb_species_id " +
                    "left join Article_Phene ap on p.phene_id = ap.phene_id " +
                    "left join Articles a on ap.article_id = a.article_id " +
                    "left join Phene_Gene pg on p.phene_id = pg.phene_id " +
                    "left join Genes_gb g on pg.gene_id = g.gene_id " +
                    "where g.gene_id is null " +
                    ") as annotations ";
    public static final String ANNOTATIONS_SELECT_COUNT =
            "select count(*) from (" + ANNOTATIONS_SELECT + ") as rowcount";
    public static final String ORDERING =
            "order by STUDY, BIOENTITY_ID, BIOENTITY_URI, PROPERTY_TYPE, PROPERTY_VALUE asc";
    public static final String ANNOTATIONS_SELECT_ALL =
            ANNOTATIONS_SELECT + ORDERING;
    public static final String ANNOTATIONS_SELECT_LIMIT =
                    "select STUDY, BIOENTITY, BIOENTITY_ID, BIOENTITY_URI, BIOENTITY_TYPE_NAME, PROPERTY_TYPE, PROPERTY_VALUE, PROPERTY_ID, SEMANTIC_TAG, ANNOTATION_DATE from (" +
                    ANNOTATIONS_SELECT_ALL +
                    ") as limiter " +
                    "limit ?, ?";
    public static final String ANNOTATIONS_SELECT_BY_STUDY =
            ANNOTATIONS_SELECT + "where STUDY = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_BIOLOGICAL_ENTITY =
            ANNOTATIONS_SELECT + "where STUDY = ? and BIOENTITY = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_PROPERTY =
            ANNOTATIONS_SELECT + "where PROPERTY_VALUE = ? " + ORDERING;
    public static final String ANNOTATIONS_SELECT_BY_PROPERTY_AND_TYPE =
            ANNOTATIONS_SELECT + "where PROPERTY_TYPE = ? and PROPERTY_VALUE = ?" + ORDERING;

    private final JDBCConventionBasedAnnotationMapper mapper;

    private JdbcTemplate jdbcTemplate;

    public OmiaAnnotationDAO() {
        this(new OmiaAnnotationFactory(new OmiaLoadingSession()));
    }

    public OmiaAnnotationDAO(OmiaAnnotationFactory annotationFactory) {
        this(new JDBCConventionBasedAnnotationMapper(annotationFactory));
    }

    public OmiaAnnotationDAO(JDBCConventionBasedAnnotationMapper annotationMapper) {
        this.mapper = annotationMapper;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override public String getDatasourceName() {
        return "omia";
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
