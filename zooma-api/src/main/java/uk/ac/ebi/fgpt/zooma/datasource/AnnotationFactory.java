package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.net.URI;
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
     * Generates a fully formed annotation object (including any associated studies, bioentities, properties, semantic
     * tags and provenance objects) given the supplied string forms.
     * <p/>
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
}
