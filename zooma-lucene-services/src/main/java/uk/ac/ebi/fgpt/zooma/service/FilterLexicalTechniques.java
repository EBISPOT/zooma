package uk.ac.ebi.fgpt.zooma.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Filter of lexical techniques (Needleman-distance and Jaccard-similarity) to select the final annotations.
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */

public class FilterLexicalTechniques {
    /**
     * The filter receives a set of annotations and selects the best annotations using several criteria (min_score,
     * num_max_annotations, pct_cutoff). These criteria can be adjusted.
     *
     * @param annotations         the annotations to filter
     * @param min_score           the minimum score
     * @param num_max_annotations
     * @param pct_cutoff
     * @return
     */
    public Map<String, Float> filterAnnotations(Map<String, Float> annotations,
                                                float min_score,
                                                int num_max_annotations,
                                                float pct_cutoff) {
        Map<String, Float> final_annotations = new HashMap<>();
        float top_score = 0.0f;
        float min_score_cut_off = 0.0f;

        if (annotations.size() > 0) {
            Map<String, Float> sortedMap = sortByComparator(annotations);
            Iterator iterator = sortedMap.entrySet().iterator();
            for (int i = 0; i < num_max_annotations; i++) {
                if (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    float score_annotation = (Float) entry.getValue();
                    if (score_annotation >= min_score_cut_off) {
                        final_annotations.put((String) entry.getKey(), score_annotation);
                        if (i == 0) {
                            top_score = score_annotation;
                            min_score_cut_off = top_score * pct_cutoff;
                        }
                    }
                }
            }
        }
        return final_annotations;
    }

    /* This method sorts a Map of annotations by score */
    private Map<String, Float> sortByComparator(Map<String, Float> unsortMap) {
        List<Map.Entry<String, Float>> list = new LinkedList<>(unsortMap.entrySet());
        // sort list based on comparator
        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                int com = o1.getValue().compareTo(o2.getValue());
                //Descending order
                return com * (-1);
            }
        });

        // put sorted list into map again
        // LinkedHashMap make sure order in which keys were inserted
        Map<String, Float> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static boolean pass_filter_by_affirmative_negative(String EFO_term, String property_value) {
        boolean isNegativeProperty = isNegative(property_value);
        boolean isNegativeEFO_term = isNegative(EFO_term);
        return isNegativeProperty == isNegativeEFO_term;
    }

    public static boolean isNegative(String sentence) {
        if (sentence.contains(" not ") || sentence.contains(" no ") || sentence.contains(" non ") ||
                sentence.contains(" not-") || sentence.contains(" no-") || sentence.contains(" non-") ||
                sentence.contains(" dont ") || sentence.contains(" don't ") || sentence.contains(" didn't ") ||
                sentence.contains(" n't ") || sentence.contains(" never ")) {
            return true;
        }
        else if (sentence.startsWith("not ") || sentence.startsWith("no ") || sentence.startsWith("non ") ||
                sentence.startsWith("not-") || sentence.startsWith("no-") || sentence.startsWith("non-") ||
                sentence.startsWith("dont ") || sentence.startsWith("don't ") || sentence.startsWith("didn't ") ||
                sentence.startsWith("n't ") || sentence.startsWith("never ")) {
            return true;
        }
        return false;
    }
}
