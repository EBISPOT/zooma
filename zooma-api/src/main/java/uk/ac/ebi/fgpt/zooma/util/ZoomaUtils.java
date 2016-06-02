package uk.ac.ebi.fgpt.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipInputStream;

/**
 * Some general ZOOMA utility functions
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class ZoomaUtils {
    public static final EncodingAlgorithm DEFAULT_ENCODING = EncodingAlgorithm.MD5;
    private static final String HEX_CHARACTERS = "0123456789ABCDEF";

    private static Logger log = LoggerFactory.getLogger(ZoomaUtils.class);

    protected static Logger getLog() {
        return log;
    }

    public static String normalizePropertyTypeString(String propertyType) {
        return propertyType.toLowerCase().replaceAll("_", " ");
    }

    public static MessageDigest generateMessageDigest() {
        return generateMessageDigest(DEFAULT_ENCODING);
    }

    public static MessageDigest generateMessageDigest(EncodingAlgorithm algorithm) {
        try {
            return MessageDigest.getInstance(algorithm.getAlgorithmName());
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    algorithm.getAlgorithmName() + " algorithm not available, this is required to generate ID");
        }
    }

    public static String generateHashEncodedID(String... contents) {
        return generateHashEncodedID(DEFAULT_ENCODING, contents);
    }

    public static String generateHashEncodedID(EncodingAlgorithm algorithm, String... contents) {
        // acquire a message digest for the given algorithm and encode
        return generateHashEncodedID(generateMessageDigest(algorithm), contents);
    }

    public static String generateHashEncodedID(MessageDigest messageDigest, String... contents) {
        return generateHashEncodedID(messageDigest, true, contents);
    }

    public static String generateHashEncodedID(MessageDigest messageDigest, boolean sortContent, String... contents) {
        if (sortContent) {
            Arrays.sort(contents);
        }

        StringBuilder idContent = new StringBuilder();
        for (String s : contents) {
            idContent.append(s);
        }
        try {
            // encode the content using the supplied message digest
            byte[] digest = messageDigest.digest(idContent.toString().getBytes("UTF-8"));

            // now translate the resulting byte array to hex
            String idKey = getHexRepresentation(digest);

            getLog().trace("Generated new " + messageDigest.getAlgorithm() + " based, hex encoded ID string: " + idKey);
            return idKey;
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported!");
        }
    }

    private static String getHexRepresentation(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEX_CHARACTERS.charAt((b & 0xF0) >> 4)).append(HEX_CHARACTERS.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    /**
     * Filter the supplied map of annotation summaries to their score, reducing them down to a set of summaries that
     * exclude any unreasonable matches.  Summaries are excluded from the results with the following criteria: <ol>
     * <li>If it duplicates a prior summary (meaning it produces a mapping to the same collection of semantic tags)</li>
     * <li>If the score for this summary is less than the value of cutoffPercentage the value of the top scoring one.
     * So, if you supply a cutoffPercentage of 0.95, only summaries with a score of 95% the value of the top hit will be
     * included.</li> </ol>
     * <p/>
     * This form does not take a minimum score - this is equivalent to calling {@link
     * #filterAnnotationSummaries(java.util.Map, float, float)} with a cutoff score of 0.
     *
     * @param summaries        the set of summaries to filter
     * @param cutoffPercentage the maximum distance away from the top score an annotation is allowed to be whilst not
     *                         being filtered
     * @return a filtered set of annotations, only including those that scored inside the confidence interval
     */
    public static List<AnnotationSummary> filterAnnotationSummaries(Map<AnnotationSummary, Float> summaries,
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
    public static List<AnnotationSummary> filterAnnotationSummaries(final Map<AnnotationSummary, Float> summaries,
                                                                   float cutoffScore,
                                                                   float cutoffPercentage) {
        Iterator<AnnotationSummary> summaryIterator = summaries.keySet().iterator();

        // we need to find summaries that agree and exclude duplicates - build a reference set
        List<AnnotationSummary> referenceSummaries = new ArrayList<>();
        referenceSummaries.add(summaryIterator.next()); // first summary can't duplicate anything

        // compare each summary with the reference set
        while (summaryIterator.hasNext()) {
            AnnotationSummary nextSummary = summaryIterator.next();
            boolean isDuplicate = false;
            AnnotationSummary shouldReplace = null;
            for (AnnotationSummary referenceSummary : referenceSummaries) {
                if (allEquals(referenceSummary.getSemanticTags(), nextSummary.getSemanticTags())) {
                    isDuplicate = true;
                    if (summaries.get(nextSummary) > summaries.get(referenceSummary)) {
                        shouldReplace = referenceSummary;
                    }
                    break;
                }
            }

            // if this doesn't duplicate another summary, add to reference set
            if (!isDuplicate) {
                referenceSummaries.add(nextSummary);
            }
            else {
                // duplicate, is the new one better?
                if (shouldReplace != null) {
                    //try and replace, keeping the order that they where placed in
                    for (int i = 0; i < referenceSummaries.size(); i++) {
                        AnnotationSummary summary = referenceSummaries.get(i);
                        if (summary.equals(shouldReplace)) {
                            referenceSummaries.remove(i);
                            referenceSummaries.add(i, nextSummary);
                        }
                    }
                }
            }
        }

        // return top scored summary
        List<AnnotationSummary> results = new ArrayList<>();
        float topScore = Collections.max(summaries.values());
        for (AnnotationSummary as : referenceSummaries) {
            float score = summaries.get(as);
            // if the score for this summary is within 5% of the top score,
            // AND if it is greater than the cutoff score
            // include
            if (score > (topScore * cutoffPercentage) && score >= cutoffScore) {
                results.add(as);
            }
        }

        //Make sure the results are sorted (highest score first).
        Collections.sort(results, new Comparator<AnnotationSummary>() {
            @Override public int compare(AnnotationSummary o1, AnnotationSummary o2) {
                return summaries.get(o2).compareTo(summaries.get(o1));
            }
        });




        return results;
    }

    public static Map<AnnotationSummary, Float> getNormalizedOLSScores(float olsTopScore, Map<AnnotationSummary, Float> summaries){

        if (summaries != null && !summaries.isEmpty()) {
            float topScore = Collections.max(summaries.values());
            float topNormalizedScore = olsTopScore;
            for (AnnotationSummary key : summaries.keySet()) {
                float score = summaries.get(key);
                float normalize = topNormalizedScore - (topScore - score);
                summaries.put(key, normalize);
            }
        }
        return summaries;
    }

    /**
     * Tests the contents of two collections to determine if they are equal.  This method will return true if and only
     * if all items in collection 1 are present in collection 2 and all items in collection 2 are present in collection
     * 1.  Furthermore, for collections that may contain duplicates (such as {@link List}s), both lists must be the same
     * length for this to be true.
     *
     * @param c1  collection 1
     * @param c2  collection 2
     * @param <T> the type of collection 1 and 2 (if either collection is typed, both collections must have the same
     *            type)
     * @return true if the contents of collection 1 and 2 are identical
     */
    public static <T> boolean allEquals(Collection<T> c1, Collection<T> c2) {
        // quick size screen for sets - if sizes aren't equal contents definitely can't be
        if (c1 instanceof Set && c2 instanceof Set) {
            if (c1.size() != c2.size()) {
                return false;
            }
        }

        // either both c1 and c2 are not a set or both sets and sizes are equal

        // is every element in c1 also in c2?
        for (T t : c1) {
            if (!c2.contains(t)) {
                return false;
            }
        }

        // and, is every element in c2 also in c1?
        for (T t : c2) {
            if (!c1.contains(t)) {
                return false;
            }
        }

        // if we get to here, all elements in each set are also in the other, so all elements are equal
        return true;
    }

    public enum EncodingAlgorithm {
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256");

        private final String algorithm;

        private EncodingAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getAlgorithmName() {
            return algorithm;
        }
    }
}
