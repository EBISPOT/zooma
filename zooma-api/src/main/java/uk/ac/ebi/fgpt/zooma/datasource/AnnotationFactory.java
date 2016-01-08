package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;

import java.net.URI;
import java.util.Collection;
import java.util.Date;

/**
 * An all-purpose factory class that can generate fully formed annotation objects and their dependants from a series of
 * strings.
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 01/10/12
 */
public interface AnnotationFactory {
    /**
     * Retrieves a name that represents the datasource that this annotation factory is capable of generating data for.
     * Although this is not treated as a unique key, it is best to take care to ensure that implementations define a
     * reasonably unique name so as to avoid confusion when users attempt to identify annotation factories using this
     * field.
     * <p>
     * If you want to categorize datasources into subsets, the recommended separator to use is a period (".") to split
     * into Java-like packages.
     *
     * @return the user-friendly name of the datasource this factory can generate annotations for
     */
    String getDatasourceName();

    /**
     * Generates a fully formed annotation object (including any associated studies, bioentities, properties, semantic
     * tags and provenance objects) given the supplied string forms.
     * <p>
     * Some of the arguments taken by this method are optional and can be null, others are required.  Annotation, Study,
     * Bioentity and Property URIs and IDs are all optional and, if absent from the dataset, can be null.  Study
     * accession is required, as is bioentity name.  It is assumed that study accessions are unique within a dataset,
     * but bioentity accessions should only be unique within a study for each dataset.  Semantic tags are required, and
     * unlike URIs and IDs (which are generated if not supplied) will be set to null if missing.
     *
     * @param annotationURI    the URI of the annotation that will be created, if present
     * @param annotationID     the ID of the annotation in the datasource.  If present, will be used to generate the
     *                         URI
     * @param studyAccession   the accession of the study.  Unique across this datasource
     * @param studyURI         the URI of the study that will be created, if present
     * @param studyID          the ID of the study in the datasource.  If present, will be used to generate the URI
     * @param studyType        the URI that specifies the type of Study e.g. an experiment or pubmed article
     * @param bioentityName    the name of the bioentity.  Should be unique per study in the datasource
     * @param bioentityURI     the URI of the bioentity that will be created, if present
     * @param bioentityID      the ID of the bioentity in the datasource.  If present, will be used to generate the URI
     * @param bioentityTypeURI the URI of the bioentity type in the datasource.
     * @param propertyType     the property type
     * @param propertyValue    the property value
     * @param propertyURI      the URI of the property that will be created, if present
     * @param propertyID       the ID of the property in the datasource.  If present, will be used to generate the URI
     * @param semanticTag      the semantic tag. Required - if null, this represents an annotation with no tag
     * @param annotator        the person or algorithm that generated this annotation. Can be null
     * @param annotationDate   the data this annotation was generated. Can be null.
     * @return the fully created annotation
     */
    Annotation createAnnotation(URI annotationURI,
                                String annotationID,
                                String studyAccession,
                                URI studyURI,
                                String studyID,
                                URI studyType,
                                String bioentityName,
                                URI bioentityURI,
                                String bioentityID,
                                String bioentityTypeName,
                                URI bioentityTypeURI,
                                String propertyType,
                                String propertyValue,
                                URI propertyURI,
                                String propertyID,
                                URI semanticTag,
                                String annotator,
                                Date annotationDate);

    /**
     * Generates a fully formed annotation object (including any associated studies, bioentities, properties, semantic
     * tags and provenance objects) given the supplied objects.
     * <p>
     * Some of the arguments taken by this method are optional and can be null, others are required. Bioentity,
     * replaces, replacedBy and semantic tags are all optional and, if absent from the dataset, can be null. Property
     * and AnnotationProvenance are required.
     *
     * @param annotatedBiologicalEntities collection of biological entities being annotated, can be null
     * @param annotatedProperty           property that form the main body of the annotation, not null
     * @param semanticTags                optional collection of semantic tags for the annotation
     * @param replaces                    collection of URI for annotations this new annotation replaces
     * @return the fully created annotation
     */
    Annotation createAnnotation(Collection<BiologicalEntity> annotatedBiologicalEntities,
                                Property annotatedProperty,
                                AnnotationProvenance annotationProvenance,
                                Collection<URI> semanticTags,
                                Collection<URI> replaces);
}
