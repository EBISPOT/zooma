package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPrediction;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPredictionTemplate;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.util.AnnotationPredictionBuilder;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Entry point for the ZOOMA application with the most commonly used functionality incorporated.  You can use this class
 * to search properties, select annotation summaries given a property or property type/value pair, and predict new
 * annotations.
 *
 * @author Tony Burdett
 * @date 14/08/15
 */
public class Zooma extends SourceFilteredEndpoint {
    private ZoomaProperties zoomaProperties;
    private ZoomaAnnotations zoomaAnnotations;
    private ZoomaAnnotationSummaries zoomaAnnotationSummaries;

    private int concurrency;
    private float cutoffScore;
    private float cutoffPercentage;

    @Autowired
    public Zooma(ZoomaProperties zoomaProperties,
                 ZoomaAnnotations zoomaAnnotations,
                 ZoomaAnnotationSummaries zoomaAnnotationSummaries,
                 @Qualifier("zoomaProperties") Properties configuration) {
        this.zoomaProperties = zoomaProperties;
        this.zoomaAnnotations = zoomaAnnotations;
        this.zoomaAnnotationSummaries = zoomaAnnotationSummaries;
        this.concurrency = Integer.parseInt(configuration.getProperty("zooma.search.concurrent.threads"));
        this.cutoffScore = Float.parseFloat(configuration.getProperty("zooma.search.significance.score"));
        this.cutoffPercentage = Float.parseFloat(configuration.getProperty("zooma.search.cutoff.score"));
    }

    public List<String> suggest(String prefix) {
        return extractPropertyValueStrings(zoomaProperties.query(prefix));
    }

    public List<Property> suggestWithType(String prefix) {
        return zoomaProperties.query(prefix);
    }

    public List<String> suggestFromSources(String prefix, URI... requiredSources) {
        return extractPropertyValueStrings(zoomaProperties.query(prefix, requiredSources));
        //        SearchType searchType = validateFilterArguments(filter);
        //        URI[] requiredSources;
        //        switch (searchType) {
        //            case REQUIRED_ONLY:
        //            case REQUIRED_AND_PREFERRED:
        //                requiredSources = parseRequiredSourcesFromFilter(filter);
        //                return extractPropertyValueStrings(zoomaProperties.query(prefix, requiredSources));
        //            case PREFERRED_ONLY:
        //            case UNRESTRICTED:
        //            default:
        //                return extractPropertyValueStrings(zoomaProperties.query(prefix));
        //        }
    }

    public List<Property> suggestWithTypeFromSources(String prefix, URI... requiredSources) {
        return zoomaProperties.query(prefix, requiredSources);
        //        SearchType searchType = validateFilterArguments(filter);
        //        URI[] requiredSources;
        //        switch (searchType) {
        //            case REQUIRED_ONLY:
        //            case REQUIRED_AND_PREFERRED:
        //                requiredSources = parseRequiredSourcesFromFilter(filter);
        //                return zoomaProperties.query(prefix, requiredSources);
        //            case PREFERRED_ONLY:
        //            case UNRESTRICTED:
        //            default:
        //                return zoomaProperties.query(prefix);
        //        }
    }

    public List<AnnotationSummary> select(String propertyValue) {
        return extractAnnotationSummaryList(zoomaAnnotationSummaries.queryAndScore(propertyValue));
    }

    public List<AnnotationSummary> select(String propertyValue, String propertyType) {
        return extractAnnotationSummaryList(zoomaAnnotationSummaries.queryAndScore(propertyValue, propertyType));
    }

    public List<AnnotationSummary> selectFromSources(String propertyValue, URI... requiredSources) {
        return extractAnnotationSummaryList(zoomaAnnotationSummaries.queryAndScore(propertyValue,
                                                                                   "",
                                                                                   Collections.<URI>emptyList(),
                                                                                   requiredSources));
    }

    public List<AnnotationSummary> selectFromSources(String propertyValue,
                                                     String propertyType,
                                                     URI... requiredSources) {
        return extractAnnotationSummaryList(zoomaAnnotationSummaries.queryAndScore(propertyValue,
                                                                                   propertyType,
                                                                                   Collections.<URI>emptyList(),
                                                                                   requiredSources));
    }

    public List<Annotation> annotate(String propertyValue) {
        Map<AnnotationSummary, Float> summaries = zoomaAnnotationSummaries.queryAndScore(propertyValue);
        return createPredictions(propertyValue, null, summaries);
    }

    public List<Annotation> annotate(String propertyValue, String propertyType) {
        Map<AnnotationSummary, Float> summaries = zoomaAnnotationSummaries.queryAndScore(propertyValue, propertyType);
        return createPredictions(propertyValue, propertyType, summaries);
    }

    public List<Annotation> annotate(String propertyValue, List<URI> preferredSources, URI... requiredSources) {
        Map<AnnotationSummary, Float> summaries = zoomaAnnotationSummaries.queryAndScore(propertyValue,
                                                                                         "",
                                                                                         preferredSources,
                                                                                         requiredSources);
        return createPredictions(propertyValue, null, summaries);
    }

    public List<Annotation> annotate(String propertyValue,
                                     String propertyType,
                                     List<URI> preferredSources,
                                     URI... requiredSources) {
        Map<AnnotationSummary, Float> summaries = zoomaAnnotationSummaries.queryAndScore(propertyValue,
                                                                                         propertyType,
                                                                                         preferredSources,
                                                                                         requiredSources);
        return createPredictions(propertyValue, propertyType, summaries);
    }

    private List<String> extractPropertyValueStrings(Collection<Property> properties) {
        List<String> result = new ArrayList<>();
        for (Property p : properties) {
            if (!result.contains(p.getPropertyValue())) {
                result.add(p.getPropertyValue());
            }
        }
        return result;
    }

    private List<AnnotationSummary> extractAnnotationSummaryList(final Map<AnnotationSummary, Float> annotationSummaryFloatMap) {
        List<AnnotationSummary> result = new ArrayList<>();
        for (AnnotationSummary as : annotationSummaryFloatMap.keySet()) {
            if (!result.contains(as)) {
                result.add(as);
            }
        }
        result.sort(new Comparator<AnnotationSummary>() {
            @Override public int compare(AnnotationSummary as1, AnnotationSummary as2) {
                return annotationSummaryFloatMap.get(as1) < annotationSummaryFloatMap.get(as2) ? -1 : 1;
            }
        });
        return result;
    }

    private List<Annotation> createPredictions(String propertyValue,
                                               String propertyType,
                                               Map<AnnotationSummary, Float> summaries) throws SearchException {
        List<Annotation> predictions = new ArrayList<>();

        // now use client to test and filter them
        if (!summaries.isEmpty()) {
            // get well scored annotation summaries
            Set<AnnotationSummary> goodSummaries = ZoomaUtils.filterAnnotationSummaries(summaries,
                                                                                        cutoffPercentage);

            // for each good summary, extract an example annotation
            boolean achievedScore = false;
            List<Annotation> goodAnnotations = new ArrayList<>();

            for (AnnotationSummary goodSummary : goodSummaries) {
                if (!achievedScore && summaries.get(goodSummary) > cutoffScore) {
                    achievedScore = true;
                }

                if (!goodSummary.getAnnotationURIs().isEmpty()) {
                    URI annotationURI = goodSummary.getAnnotationURIs().iterator().next();
                    Annotation goodAnnotation = zoomaAnnotations.getAnnotationService().getAnnotation(annotationURI);
                    if (goodAnnotation != null) {
                        goodAnnotations.add(goodAnnotation);
                    }
                    else {
                        throw new SearchException(
                                "An annotation summary referenced an annotation that " +
                                        "could not be found - ZOOMA's indexes may be out of date");
                    }
                }
                else {
                    String message = "An annotation summary with no associated annotations was found - " +
                            "this is probably an error in inferring a new summary from lexical matches";
                    getLog().warn(message);
                    throw new SearchException(message);
                }
            }

            // now we have a list of good annotations; use this list to create predicted annotations
            AnnotationPrediction.Confidence confidence;
            if (goodAnnotations.size() == 1 && achievedScore) {
                // one good annotation, so create prediction with high confidence
                confidence = AnnotationPrediction.Confidence.HIGH;
            }
            else {
                if (achievedScore) {
                    // multiple annotations each with a good score, create predictions with good confidence
                    confidence = AnnotationPrediction.Confidence.GOOD;
                }
                else {
                    if (goodAnnotations.size() == 1) {
                        // single stand out annotation that didn't achieve score, create prediction with good confidence
                        confidence = AnnotationPrediction.Confidence.GOOD;
                    }
                    else {
                        // multiple annotations, none reached score, so create prediction with medium confidence
                        confidence = AnnotationPrediction.Confidence.MEDIUM;
                    }
                }
            }

            // ... code to create new annotation predictions goes here
            for (Annotation annotation : goodAnnotations) {
                AnnotationPredictionTemplate pt = AnnotationPredictionBuilder.buildPrediction(annotation);
                if (propertyType == null) {
                    pt.searchWas(propertyValue);
                }
                else {
                    pt.searchWas(propertyValue, propertyType);
                }
                pt.confidenceIs(confidence);
                predictions.add(pt.build());
            }
        }

        return predictions;
    }
}
