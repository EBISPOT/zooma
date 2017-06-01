package uk.ac.ebi.spot.zooma.utils.predictor;

import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.net.URI;
import java.util.*;


/**
 * Some general ZOOMA utility functions
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class PredictorUtils {

    /**
     * Filter the supplied map of annotation summaries to their score, reducing them down to a set of summaries that
     * exclude any unreasonable matches.  Summaries are excluded from the results with the following criteria: <ol>
     * <li>If it duplicates a prior summary (meaning it produces a mapping to the same collection of semantic tags)</li>
     * <li>If the score for this summary is less than the value of cutoffPercentage the value of the top scoring one.
     * So, if you supply a cutoffPercentage of 0.95, only summaries with a score of 95% the value of the top hit will be
     * included.</li> </ol>
     * <p/>
     * This form does not take a minimum score - this is equivalent to calling {@link
     * #filterAnnotationSummaries(Map, float, float)} with a cutoff score of 0.
     *
     * @param summaries        the set of summaries to filter
     * @param cutoffPercentage the maximum distance away from the top score an annotation is allowed to be whilst not
     *                         being filtered
     * @return a filtered set of annotations, only including those that scored inside the confidence interval
     */
    public static List<AnnotationPrediction> filterAnnotationSummaries(Map<AnnotationPrediction, Float> summaries,
                                                                    float cutoffPercentage) {
        return filterAnnotationSummaries(summaries, 0, cutoffPercentage);

    }


    /**
     * Filter the supplied map of annotation summaries to their score, reducing them down to a set of summaries that
     * exclude any unreasonable matches.  Summaries are excluded from the results with the following criteria: <ol>
     * <li>If it duplicates a prior summary (meaning it produces a mapping to the same collection of semantic tags)</li>
     * <li>If the score for this summary is less than the value of cutoffPercentage the value of the top scoring one.
     * So, if you supply a cutoffPercentage of 0.95, only summaries with a score of 95% the value of the top hit will be
     * included.</li> </ol>
     *
     * @param summaries        the set of summaries to filter
     * @param cutoffScore      the minimum allowed score for an annotation to not be filtered
     * @param cutoffPercentage the maximum
     * @return a filtered set of annotations, only including those that scored inside the confidence interval
     */
    public static List<AnnotationPrediction> filterAnnotationSummaries(final Map<AnnotationPrediction, Float> summaries,
                                                                    float cutoffScore,
                                                                    float cutoffPercentage) {
        if(summaries.isEmpty()){
            return new ArrayList<>();
        }
        Iterator<AnnotationPrediction> summaryIterator = summaries.keySet().iterator();

        // we need to find summaries that agree and exclude duplicates - build a reference set
        List<AnnotationPrediction> referenceSummaries = new ArrayList<>();
        referenceSummaries.add(summaryIterator.next()); // first summary can't duplicate anything

        // compare each summary with the reference set
        while (summaryIterator.hasNext()) {
            AnnotationPrediction nextSummary = summaryIterator.next();
            referenceSummaries.add(nextSummary);
        }

        // return top scored summary
        List<AnnotationPrediction> results = new ArrayList<>();
        float topScore = Collections.max(summaries.values());
        for (AnnotationPrediction as : referenceSummaries) {
            float score = summaries.get(as);
            // if the score for this summary is within 5% of the top score,
            // AND if it is greater than the cutoff score
            // include
            if (score > (topScore * cutoffPercentage) && score >= cutoffScore) {
                results.add(as);
            }
        }

        //Make sure the results are sorted (highest score first).
        Collections.sort(results, new Comparator<AnnotationPrediction>() {
            @Override public int compare(AnnotationPrediction o1, AnnotationPrediction o2) {
                return summaries.get(o2).compareTo(summaries.get(o1));
            }
        });

        return results;
    }


    /*
     * Indicates whether a set of sources contains the None Selected checkbox, or if they are empty. If so then these sources
     * should not be searched.
     */
    public static  boolean shouldSearch(List<String> sources) {
        if(sources == null){
            return true;
        }
        for (String source : sources) {
            if (source.equals("none")) { //source.toString().equals("None") || source.toString().equals("none") || source.toString().equals("Select None")
                return false;
            }
        }
        return true;
    }

}
