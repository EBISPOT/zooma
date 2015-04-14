package uk.ac.ebi.fgpt.zooma.io;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An implementation of a resolver service that uses the context of each annotation (biological entities, studies and
 * property types) to infer whether two annotations resolve or not.  This class requires biological entity and
 * annotation DAOs to perform lookups.
 * <p/>
 * To annotations are considered to resolve if either a) they have the same URI as an existing annotation or b) they
 * share matching study accessions, biological entity names, and property types.  If these checks pass, then {@link
 * #resolve(uk.ac.ebi.fgpt.zooma.model.Annotation)} will return a reference to the annotation from the DAO. It is also
 * possible to perform an update check to see if the property value or semantic tag on the new annotation has been
 * altered with respect to the annotation that was acquired from the DAO, using (for example) {@link
 * #isUpdated(uk.ac.ebi.fgpt.zooma.model.Annotation, uk.ac.ebi.fgpt.zooma.model.Annotation)}.
 * <p/>
 * By default, this will create a parallelized resolver implementation with 32 worker threads.
 *
 * @author Tony Burdett
 * @date 03/06/13
 */
@Deprecated
public class TagBasedAnnotationResolver extends AbstractParallelAnnotationResolver {
    public TagBasedAnnotationResolver() {
        super(32);
    }

    public TagBasedAnnotationResolver(int numberOfThreads) {
        super(numberOfThreads);
    }

    @Override public boolean wasModified(Annotation annotation) {
        return findModified(annotation) != null;
    }

    @Override public Annotation findModified(Annotation annotation) {
        // try to identify if this might be a related annotation...
        getLog().trace("Doing modification check on " + annotation.getURI());

        // we consider annotations to be related if...
        // 1) properties match,
        // 2) property types match, as long as there is only one type
        // 3) property values for untyped properties match

        // get all annotations which map to the same semantic tag as this one
        Collection<Annotation> referenceAnnotations = new HashSet<>();
        for (URI referenceSemanticTag : annotation.getSemanticTags()) {
            getLog().trace("Fetching candidate annotations for semantic tag " + referenceSemanticTag + "...");
            Collection<Annotation> candidates =
                    getZoomaAnnotationDAO().readBySemanticTag(referenceSemanticTag);

            // for our candidates, discard any with a different source
            Iterator<Annotation> candidateIterator = candidates.iterator();
            while (candidateIterator.hasNext()) {
                Annotation candidate = candidateIterator.next();
                URI sourceURI = candidate.getProvenance().getSource().getURI();
                AnnotationSource.Type sourceType = candidate.getProvenance().getSource().getType();
                if (!sourceURI.equals(annotation.getProvenance().getSource().getURI()) ||
                        !sourceType.equals(annotation.getProvenance().getSource().getType())) {
                    candidateIterator.remove();
                }
            }

            if (candidates.size() > 0) {
                getLog().trace("Modification check on " + annotation.getURI() + ": " +
                                       "semantic tag match on " + referenceSemanticTag + ", " +
                                       candidates.size() + " candidate annotations");
                referenceAnnotations.addAll(candidates);
            }
        }

        Property property = annotation.getAnnotatedProperty();
        String propertyType;
        if (property instanceof TypedProperty) {
            String unnormalized = ((TypedProperty) property).getPropertyType();
            propertyType = ZoomaUtils.normalizePropertyTypeString(unnormalized);
        }
        else {
            propertyType = "[UNTYPED]";
        }

        Set<Annotation> typeMatchedReferenceAnnotations = new HashSet<>();
        for (Annotation referenceAnnotation : referenceAnnotations) {
            Property referenceProperty = referenceAnnotation.getAnnotatedProperty();
            if (referenceProperty instanceof TypedProperty) {
                String referencePropertyType =
                        ZoomaUtils.normalizePropertyTypeString(((TypedProperty) referenceProperty).getPropertyType());
                if (referencePropertyType.equalsIgnoreCase(propertyType)) {
                    getLog().trace("Modification check on " + annotation.getURI() + ": " +
                                           "property types match on " + referencePropertyType);
                    typeMatchedReferenceAnnotations.add(referenceAnnotation);
                }
            }
        }

        // if there are zero, or more than one, annotation with matching type for this semantic tag,
        // we can't find modified annotation
        if (typeMatchedReferenceAnnotations.size() == 0 || typeMatchedReferenceAnnotations.size() > 1) {
            getLog().debug("Of " + referenceAnnotations.size() + " candidate annotations, " +
                                   "we found " + typeMatchedReferenceAnnotations.size() + " annotations that " +
                                   "match on property type, so no links will be created");
            return null;
        }
        else {
            // single property-type matched reference annotation, return this
            getLog().debug("Found " + typeMatchedReferenceAnnotations.size() + " annotations with matching " +
                                   "semantic tag + property type");
            return typeMatchedReferenceAnnotations.iterator().next();
        }
    }

    private Collection<URI> findReferenceSemanticTags(Annotation annotation) {
        return annotation.getSemanticTags();
    }
}
