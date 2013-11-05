package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class handles the processing of strings with brackets.
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */
public class SearchStringProcessor_Brackets implements SearchStringProcessor {
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
    public boolean canProcess(String searchString, String searchStringType) {
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
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        int pos_ini = searchString.indexOf("(");
        int pos_fin = searchString.indexOf(")");

        // extract the "content" - i.e. everything from opening to closing brackets
        String content = "\\" + searchString.substring(pos_ini, pos_fin) + "\\)";
        content = content.replaceAll("\\+", "\\\\+");
        content = content.replaceAll("\\*", "\\\\*");

        // and replace it with a single whitespace
        String processedString = searchString.replaceAll(content, " ");

        // remove extraneous whitespace
        processedString = processedString.trim().replaceAll(" +", " ");
        // return processed string, only if it is different from the original
        if (!processedString.contentEquals(searchString.toLowerCase())) {
            return Collections.singletonList(processedString);
        }
        else {
            return Collections.emptyList();
        }
    }
}