
package uk.ac.ebi.fgpt.zooma.service;


import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;


/**
 * Normalizer of EFO labels and properties. This normalizer removes stopwords (e.g. "the", "a")  and characters (e.g.
 * "-", ",") Normalization improves quality of lexical mappings. Normalization should be applied to EFO labels and
 * properties before using approximate lexical techniques.
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */

public class NormalizerLexicalTechniques {
    public Collection<String> stopWords;

    public void init() {
        stopWords = new ArrayList<>();
        stopWords.add("a");
        stopWords.add("an");
        stopWords.add("are");
        stopWords.add("as");
        stopWords.add("at");
        stopWords.add("be");
        stopWords.add("but");
        stopWords.add("by");
        stopWords.add("for");
        stopWords.add("if");
        stopWords.add("in");
        stopWords.add("into");
        stopWords.add("is");
        stopWords.add("it");
        stopWords.add("of");
        stopWords.add("on");
        stopWords.add("such");
        stopWords.add("that");
        stopWords.add("the");
        stopWords.add("their");
        stopWords.add("then");
        stopWords.add("there");
        stopWords.add("these");
        stopWords.add("they");
        stopWords.add("this");
        stopWords.add("to");
        stopWords.add("was");
        stopWords.add("will");
        stopWords.add("with");
        stopWords.add("nos");
    }

    /**
     * This method removes the stopwords of a string
     *
     * @param input the string to remove stopwords from
     * @return the processed string
     */
    public String removeStopWords(String input) {
        String output = "";
        String[] inputWords = input.split(" ");

        for (String inputWord : inputWords) {
            if (!stopWords.contains(inputWord)) {
                output += inputWord + " ";
            }
        }

        // remove extraneous whitespace
        return output.trim().replaceAll(" +", " ");
    }

    /**
     * This method removes some separator characters (e.g. "," "-" "_") of a string
     *
     * @param input the string to remove separator characters from
     * @return the processed string
     */
    public String removeCharacters(String input) {
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