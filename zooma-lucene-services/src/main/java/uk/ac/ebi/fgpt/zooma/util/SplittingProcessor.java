package uk.ac.ebi.fgpt.zooma.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A string processor that can split strings into it's constituent elements if it consists of two distinct parts,
 * separated by 'and'
 *
 * @author Jose Iglesias
 * @date 12/8/13
 */
public class SplittingProcessor implements SearchStringProcessor {
    @Override
    public float getBoostFactor() {
        return 0.7f;
    }

    /**
     * Returns true if the property value contains exactly one " and ". Returns false otherwise.
     *
     *
     * @param searchString     the search string to test
     * @return true if the string can be processed
     */
    @Override
    public boolean canProcess(String searchString) {
        return StringUtils.countMatches(searchString, " and ") == 1;
    }

    @Override
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        ArrayList<String> processedStrings = new ArrayList<>();
        String[] expressions = searchString.split(" and ");
        if (expressions != null && expressions.length == 2) {
            Collections.addAll(processedStrings, expressions);
        }
        return processedStrings;
    }
}