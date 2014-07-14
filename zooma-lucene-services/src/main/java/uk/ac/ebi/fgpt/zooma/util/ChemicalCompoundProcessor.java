package uk.ac.ebi.fgpt.zooma.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles the processing of properties of type "compound".
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */
public class ChemicalCompoundProcessor extends AbstractDictionaryLoadingProcessor {
    // units dictionary contains all subclasses of "concentration unit" (UO_0000051).
    public ChemicalCompoundProcessor(String dictionaryResourceName) {
        super(dictionaryResourceName);
    }

    public ChemicalCompoundProcessor(Resource dictionaryResource) {
        super(dictionaryResource);
    }

    @Override
    public float getBoostFactor() {
        return 0.95f;
    }

    /**
     * Takes a string, looks for numbers and concentration units in the string, removes them and returns the processed
     * strings. Normally, one string is returned.
     */
    @Override
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        String processedString = searchString;

        // space is important in order not to remove numbers within compounds.. (e.g indole-3-acetic acid)
        String space = "\\s";
        // pattern for number: int or float..
        String number_float = "\\d{1,10}.\\d{1,10}" + space;
        String number_int = "\\d{1,10}" + space;

        Pattern pattern_number_float = Pattern.compile(number_float);
        Pattern pattern_number_int = Pattern.compile(number_int);

        Matcher matcher_number_float = pattern_number_float.matcher(searchString);
        Matcher matcher_number_int = pattern_number_int.matcher(searchString);

        String substring_number = null;
        if (matcher_number_float.find()) {
            substring_number = matcher_number_float.group();
        }
        else if (matcher_number_int.find()) {
            substring_number = matcher_number_int.group();
        }

        // remove any detected number
        if (substring_number != null) {
            processedString = searchString.replaceAll(substring_number, " ");
        }

        // remove any units at the end of this string
        boolean removed_unit = false;
        for (String unit : getDictionary()) {
            if (processedString.contains(" " + unit + " ") ||
                    processedString.startsWith(unit + " ") ||
                    processedString.endsWith(" " + unit)) {
                processedString = processedString.replaceAll(unit, " ");
                removed_unit = true;
            }
        }

        //Sometimes units within compounds are in plural (e.g: metformin 50 milligrams per kilogram)
        //So, a more flexible/approximate mapping for units is included
        if (!removed_unit) {
            ArrayList<String> substrings = extractSubstrings(processedString);
            for (String substring : substrings) {
                if (levenshteinMatches(substring, getDictionary())) {
                    processedString = processedString.replaceAll(substring, " ");
                    break;
                }
            }
        }

        // remove extraneous whitespace
        processedString = processedString.trim().replaceAll(" +", " ");
        // return processed string, only if it is different from the original
        if (!processedString.contentEquals(searchString)) {
            return Collections.singletonList(processedString);
        }
        else {
            return Collections.emptyList();
        }
    }

    /**
     * Extract substrings of the original property. These strings potentially contain the concentration unit
     */
    private ArrayList<String> extractSubstrings(String processedString) {
        String words[] = StringUtils.split(processedString);

        ArrayList<String> possibleUnits = new ArrayList<>();
        ArrayList<String> possibleUnits_notEmpty = new ArrayList<>();

        if (words.length > 1) {
            possibleUnits.add(StringUtils.join(words, " ", 1, words.length));
            possibleUnits.add(StringUtils.join(words, " ", 2, words.length));
            possibleUnits.add(StringUtils.join(words, " ", 3, words.length));
            possibleUnits.add(StringUtils.join(words, " ", 0, words.length - 1));
            possibleUnits.add(StringUtils.join(words, " ", 0, words.length - 2));
            possibleUnits.add(StringUtils.join(words, " ", 0, words.length - 3));
        }

        for (String s : possibleUnits) {
            if (!s.isEmpty()) {
                possibleUnits_notEmpty.add(s);
            }
        }

        return possibleUnits_notEmpty;
    }

    /**
     * Check if substring matches approximately to any concentration unit The method uses LevenshteinDistance. Only 1
     * edition/change between strings is permissible to consider that there is an approximate matching.
     */
    private boolean levenshteinMatches(String substring, Set<String> units) {
        for (String unit : units) {
            if (unit.length() > 2) {  //Exclude abbreviations/acronyms units
                if (StringUtils.getLevenshteinDistance(substring, unit, 1) != -1) {
                    return true;
                }
            }
        }
        return false;
    }
}