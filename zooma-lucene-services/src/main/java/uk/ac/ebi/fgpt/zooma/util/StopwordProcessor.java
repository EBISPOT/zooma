package uk.ac.ebi.fgpt.zooma.util;

import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * A {@link SearchStringProcessor} that removes stopwords from the strings it is given to process.  Lucene normally does
 * this for searches by default, but as they convey meaning in ontology annotations we usually require first searching
 * with stopwords present and therefore we store stopwords in the index.  This class is therefore a workaround to allow
 * results that return no results with stopwords included to have them stripped and requeried.
 * <p/>
 * This technique is usually applied to ontology labels before using approximate matching algorithms
 *
 * @author Tony Burdett
 * @author Jose Iglesias
 * @date 11/11/13
 */
public class StopwordProcessor extends AbstractDictionaryLoadingProcessor {
    public StopwordProcessor(String dictionaryResourceName) {
        super(dictionaryResourceName);
    }

    public StopwordProcessor(Resource dictionaryResource) {
        super(dictionaryResource);
    }

    @Override public float getBoostFactor() {
        return 1;
    }

    @Override public Collection<String> processSearchString(String searchString) throws IllegalArgumentException {
        return Collections.singleton(removeCharacters(removeStopWords(searchString.toLowerCase())));
    }

    /**
     * This method removes the stopwords of a string
     *
     * @param input the string to remove stopwords from
     * @return the processed string
     */
    private String removeStopWords(String input) {
        String output = "";
        String[] inputWords = input.split(" ");
        for (String inputWord : inputWords) {
            if (!getDictionary().contains(inputWord) && inputWord != null && !inputWord.isEmpty()) {
                output += inputWord + " ";
            }
        }

        // remove extraneous whitespace
        return output.trim();
    }

    /**
     * This method removes some separator characters (e.g. "," "-" "_") of a string
     *
     * @param input the string to remove separator characters from
     * @return the processed string
     */
    private String removeCharacters(String input) {
        String output = input.replaceAll(", ", " ");
        output = output.replaceAll(" _ ", " ");
        output = output.replaceAll(" - ", " ");

        //Brackets of compounds shouldn't be removed  (e.g: 4-(N-nitrosomethylamino)-1-(3-pyridyl)butan-1-one  )
        //Two patterns try to discover compounds..
        String pattern1 = ".{0,100}\\(.{1,100}\\)\\S{1,100}.{0,100}";
        String pattern2 = ".{0,100}\\S{1,100}\\(.{1,100}\\).{0,100}";

        //if brackets don't belong to a compound then they are removed
        if (!(Pattern.matches(pattern1, input) || Pattern.matches(pattern2, input))) {
            output = output.replaceAll(" \\(", " ");
            output = output.replaceAll("\\) ", " ");
            if (output.endsWith(")")) {
                output = output.substring(0, output.length() - 1);
            }
        }
        return output;
    }
}
