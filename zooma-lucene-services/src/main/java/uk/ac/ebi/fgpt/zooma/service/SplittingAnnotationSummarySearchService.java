package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSummary;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class SplittingAnnotationSummarySearchService extends AnnotationSummarySearchServiceDecorator {

    SearchStringProcessor_Splitter SearchStringProcessor_Splitter;

    public uk.ac.ebi.fgpt.zooma.service.SearchStringProcessor_Splitter getSearchStringProcessor_Splitter() {
        return SearchStringProcessor_Splitter;
    }

    public void setSearchStringProcessor_Splitter(uk.ac.ebi.fgpt.zooma.service.SearchStringProcessor_Splitter SearchStringProcessor_Splitter) {
        this.SearchStringProcessor_Splitter = SearchStringProcessor_Splitter;
    }

    public SplittingAnnotationSummarySearchService(AnnotationSummarySearchService annotationSummarySearchService) {
        super(annotationSummarySearchService);
    }

    @Override
    public Map<AnnotationSummary, Float> searchAndScore_QueryExpansion(String propertyValuePattern) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Initialization failed, cannot query", e);
        }

        getLog().debug("SplittingAnnotationSummarySearchService: " + propertyValuePattern);
        getLog().debug("Calling searchAndScore... ");
        Map<AnnotationSummary, Float> completeString_results =
                super.searchAndScore_QueryExpansion(propertyValuePattern);

        // If the complete string hasn't annotations then partialStrings are used
        if (completeString_results.isEmpty()) {
            // We check if string contains "and"
            if (getSearchStringProcessor_Splitter().canProcess(propertyValuePattern, null)) {
                float boost_partial_strings = 0.7f;

                List<String> parts = getSearchStringProcessor_Splitter().processSearchString(propertyValuePattern);

                //For now, we only work with properties containing 2 sentences, such as "sentence1 and sentence2"
                if (parts != null && parts.size() == 2) {
                    Map<String, Map<AnnotationSummary, Float>> partResults = new HashMap<>();
                    for (String partString : parts) {
                        Map<AnnotationSummary, Float> partString_results =
                                super.searchAndScore_QueryExpansion(partString);
                        partResults.put(partString, partString_results);
                    }

                    if (partResults.size() == 2) {
                        int size_string1 = parts.get(0).length();
                        int size_string2 = parts.get(1).length();

                        for (Map.Entry<AnnotationSummary, Float> annotationSummaryFloatEntry : partResults.get(
                                parts.get(0)).entrySet()) {

                            Map.Entry ent1 = (Map.Entry) annotationSummaryFloatEntry;
                            AnnotationSummary anSummary1 = (AnnotationSummary) ent1.getKey();
                            Float score1 = (Float) ent1.getValue();
                            String type1 = anSummary1.getAnnotatedPropertyType();

                            for (Map.Entry<AnnotationSummary, Float> annotationSummaryFloatEntry1 : partResults.get(
                                    parts.get(1)).entrySet()) {

                                Map.Entry ent2 = (Map.Entry) annotationSummaryFloatEntry1;
                                AnnotationSummary anSummary2 = (AnnotationSummary) ent2.getKey();
                                Float score2 = (Float) ent2.getValue();

                                String type2 = anSummary2.getAnnotatedPropertyType();

                                float scoreFinal = ((size_string1 * score1 + size_string2 * score2) /
                                        (size_string1 + size_string2)) * boost_partial_strings;
                                float qualityScoreFinal = ((size_string1 * anSummary1.getQualityScore() +
                                        size_string2 * anSummary2.getQualityScore()) / (size_string1 + size_string2)) *
                                        boost_partial_strings;


                                HashSet<URI> aggregation_semanticTags = new HashSet<URI>();
                                HashSet<URI> aggregation_annotationURIs = new HashSet<URI>();

                                Collection<URI> anSummary1_semanticTags = anSummary1.getSemanticTags();
                                Collection<URI> anSummary2_semanticTags = anSummary2.getSemanticTags();
                                Collection<URI> anSummary1_annotationURIs = anSummary1.getAnnotationURIs();
                                Collection<URI> anSummary2_annotationURIs = anSummary2.getAnnotationURIs();


                                if (anSummary1_semanticTags != null && !anSummary1_semanticTags.isEmpty()) {
                                    for (URI anSummary1_semanticTag : anSummary1_semanticTags) {
                                        aggregation_semanticTags.add(anSummary1_semanticTag);
                                    }
                                }

                                if (anSummary2_semanticTags != null && !anSummary2_semanticTags.isEmpty()) {
                                    for (URI anSummary2_semanticTag : anSummary2_semanticTags) {
                                        aggregation_semanticTags.add(anSummary2_semanticTag);
                                    }
                                }

                                if (anSummary1_annotationURIs != null && !anSummary1_annotationURIs.isEmpty()) {
                                    for (URI anSummary1_annotationURI : anSummary1_annotationURIs) {
                                        aggregation_annotationURIs.add(anSummary1_annotationURI);
                                    }
                                }


                                if (anSummary2_annotationURIs != null && !anSummary2_annotationURIs.isEmpty()) {
                                    for (URI anSummary2_annotationURI : anSummary2_annotationURIs) {
                                        aggregation_annotationURIs.add(anSummary2_annotationURI);
                                    }
                                }

                                String type = null;

                                if (type1.contentEquals(type2)) {
                                    type = type1;
                                }

                                AnnotationSummary newAnnotationSummary = new SimpleAnnotationSummary(null,
                                                                                                     type,
                                                                                                     propertyValuePattern,
                                                                                                     aggregation_semanticTags,
                                                                                                     aggregation_annotationURIs,
                                                                                                     qualityScoreFinal);

                                completeString_results.put(newAnnotationSummary, scoreFinal);
                            }
                        }
                    }
                }
            }
        }
        return completeString_results;
    }

    @Override
    public Map<AnnotationSummary, Float> searchAndScore_QueryExpansion(String propertyType,
                                                                       String propertyValuePattern) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Initialization failed, cannot query", e);
        }

        getLog().debug("SplittingAnnotationSummarySearchService: " + propertyValuePattern);
        getLog().debug("Calling searchAndScore... ");
        Map<AnnotationSummary, Float> completeString_results =
                super.searchAndScore_QueryExpansion(propertyType, propertyValuePattern);

        // If the complete string hasn't annotations then partial strings are used
        if (completeString_results.isEmpty()) {
            // We check if string contains "and"
            if (getSearchStringProcessor_Splitter().canProcess(propertyValuePattern, null)) {
                float boost_partial_strings = 0.7f;

                List<String> partsString =
                        getSearchStringProcessor_Splitter().processSearchString(propertyValuePattern);

                //For now, we only work with properties containing 2 sentences, such as "sentence1 and sentence2"
                if (partsString != null && partsString.size() == 2) {
                    Map<String, Map<AnnotationSummary, Float>> partResults = new HashMap<>();

                    for (String partString : partsString) {
                        Map<AnnotationSummary, Float> partString_results =
                                super.searchAndScore_QueryExpansion(propertyType, partString);
                        partResults.put(partString, partString_results);
                    }

                    if (partResults.size() == 2) {
                        int size_string1 = partsString.get(0).length();
                        int size_string2 = partsString.get(1).length();

                        for (Map.Entry<AnnotationSummary, Float> annotationSummaryFloatEntry : partResults.get(
                                partsString.get(0)).entrySet()) {
                            Map.Entry ent1 = (Map.Entry) annotationSummaryFloatEntry;
                            AnnotationSummary anSummary1 = (AnnotationSummary) ent1.getKey();
                            Float score1 = (Float) ent1.getValue();

                            Iterator it2 = partResults.get(partsString.get(1)).entrySet().iterator();
                            String type1 = anSummary1.getAnnotatedPropertyType();

                            while (it2.hasNext()) {
                                Map.Entry ent2 = (Map.Entry) it2.next();
                                AnnotationSummary anSummary2 = (AnnotationSummary) ent2.getKey();
                                Float score2 = (Float) ent2.getValue();

                                String type2 = anSummary2.getAnnotatedPropertyType();

                                float scoreFinal = ((size_string1 * score1 + size_string2 * score2) /
                                        (size_string1 + size_string2)) * boost_partial_strings;
                                float qualityScoreFinal = ((size_string1 * anSummary1.getQualityScore() +
                                        size_string2 * anSummary2.getQualityScore()) / (size_string1 + size_string2)) *
                                        boost_partial_strings;

                                HashSet<URI> aggregation_semanticTags = new HashSet<>();
                                HashSet<URI> aggregation_annotationURIs = new HashSet<>();

                                Collection<URI> anSummary1_semanticTags = anSummary1.getSemanticTags();
                                Collection<URI> anSummary2_semanticTags = anSummary2.getSemanticTags();
                                Collection<URI> anSummary1_annotationURIs = anSummary1.getAnnotationURIs();
                                Collection<URI> anSummary2_annotationURIs = anSummary2.getAnnotationURIs();

                                if (anSummary1_semanticTags != null && !anSummary1_semanticTags.isEmpty()) {
                                    for (URI anSummary1_semanticTag : anSummary1_semanticTags) {
                                        aggregation_semanticTags.add(anSummary1_semanticTag);
                                    }
                                }

                                if (anSummary2_semanticTags != null && !anSummary2_semanticTags.isEmpty()) {
                                    for (URI anSummary2_semanticTag : anSummary2_semanticTags) {
                                        aggregation_semanticTags.add(anSummary2_semanticTag);
                                    }
                                }

                                if (anSummary1_annotationURIs != null && !anSummary1_annotationURIs.isEmpty()) {
                                    for (URI anSummary1_annotationURI : anSummary1_annotationURIs) {
                                        aggregation_annotationURIs.add(anSummary1_annotationURI);
                                    }
                                }


                                if (anSummary2_annotationURIs != null && !anSummary2_annotationURIs.isEmpty()) {
                                    for (URI anSummary2_annotationURI : anSummary2_annotationURIs) {
                                        aggregation_annotationURIs.add(anSummary2_annotationURI);
                                    }
                                }

                                String type = null;

                                if (type1.contentEquals(type2)) {
                                    type = type1;
                                }

                                AnnotationSummary newAnnotationSummary = new SimpleAnnotationSummary(null,
                                                                                                     type,
                                                                                                     propertyValuePattern,
                                                                                                     aggregation_semanticTags,
                                                                                                     aggregation_annotationURIs,
                                                                                                     qualityScoreFinal);

                                completeString_results.put(newAnnotationSummary, scoreFinal);
                            }
                        }
                    }
                }
            }
        }
        return completeString_results;
    }
}