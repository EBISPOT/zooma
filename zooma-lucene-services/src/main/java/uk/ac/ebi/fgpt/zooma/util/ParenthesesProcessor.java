package uk.ac.ebi.fgpt.zooma.util;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles the processing of strings with brackets.
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */
public class ParenthesesProcessor implements SearchStringProcessor {
    @Override
    public float getBoostFactor() {
        return 0.95f;
    }

    /**
     * Returns true if the property value contains brackets and they don't belong to a compound. Brackets of compounds
     * mustn't be removed, e.g: 4-(N-nitrosomethylamino)-1-(3-pyridyl)butan-1-one
     * <p/>
     * Returns false otherwise.
     */
    @Override
    public boolean canProcess(String searchString) {
        if (searchString.contains("(") && searchString.contains(")")) {
            // Brackets of compounds mustn't be removed (e.g: 4-(N-nitrosomethylamino)-1-(3-pyridyl)butan-1-one  )
            // Two patterns to identify compounds:
            String pattern1_compound = ".{0,100}\\(.{1,100}\\)\\S{1,100}.{0,100}";
            String pattern2_compound = ".{0,100}\\S{1,100}\\(.{1,100}\\).{0,100}";

            // Check if string would be a compound..
            if (!(Pattern.matches(pattern1_compound, searchString) ||
                    Pattern.matches(pattern2_compound, searchString))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Takes a string and removes the brackets and its content
     */
    @Override

    public List<String> processSearchString(String searchString) throws IllegalArgumentException, InterruptedException {
        String processedString = searchString;

//a group starting with
//     \\( => starting with one (
//     [^\\)]* => followed by any character but not a closing ) 0 or more times
//     \\) => followed by a closing parenthesis )
        Pattern p = Pattern.compile("(\\([^\\)]*\\))");

        Matcher m = p.matcher(processedString);
        while (m.find()) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Interrupted whilst processing search string '" + searchString + "'");
            }

            int pos_ini = m.start(1);
            int pos_fin = m.end(1);
            if (pos_ini < pos_fin) {
                // extract the "content" - i.e. everything from opening to closing brackets
                processedString =  processedString.substring(0,pos_ini) + processedString.substring(pos_fin, processedString.length());
            }
        }
        // remove extraneous whitespace
        processedString = processedString.trim();
        // return processed string, only if it is different from the original
        if (!processedString.contentEquals(searchString)) {
            return Collections.singletonList(processedString);
        }
        else {
            return Collections.emptyList();
        }
    }

}