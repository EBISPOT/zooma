package uk.ac.ebi.fgpt.zooma.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles the processing of properties of type "compound".
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */

public class SearchStringProcessor_Compounds implements SearchStringProcessor {
    // units contains all subclasses of "concentration unit" (UO_0000051).
    private ArrayList<String> units;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override
    public float getBoostFactor() {
        return 0.95f;
    }

    /**
     * Returns true if the property type is equal to compound or growth_condition. Returns false otherwise.
     */
    @Override
    public boolean canProcess(String searchString, String searchStringType) {
        if (searchStringType != null && !searchStringType.isEmpty()) {
            searchStringType = searchStringType.toLowerCase();
            if (searchStringType.contentEquals("compound") || searchStringType.contentEquals("compounds") ||
                    searchStringType.contentEquals("growth condition") ||
                    searchStringType.contentEquals("growth_condition")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Takes a string, looks for numbers and concentration units in the string, removes them and returns the processed
     * strings. Normally, one string is returned.
     */
    @Override
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        String processedString = searchString.toLowerCase();

        // space is important in order not to remove numbers within compounds.. (e.g indole-3-acetic acid)
        String space = "\\s{1}";
        // pattern for number: int or float..
        String number_float = "\\d{1,10}.\\d{1,10}" + space;
        String number_int = "\\d{1,10}" + space;

        Pattern pattern_number_float = Pattern.compile(number_float);
        Pattern pattern_number_int = Pattern.compile(number_int);

        Matcher matcher_number_float = pattern_number_float.matcher(searchString);
        Matcher matcher_number_int = pattern_number_int.matcher(searchString);

        String substring_number = null;
        if (matcher_number_float != null && matcher_number_float.find()) {
            substring_number = matcher_number_float.group();
        }
        else if (matcher_number_int != null && matcher_number_int.find()) {
            substring_number = matcher_number_int.group();
        }

        // remove any detected number
        if (substring_number != null) {
            processedString = searchString.replaceAll(substring_number, " ");
        }

        // remove any units at the end of this string
        boolean removed_unit = false;
        for (String unit : getUnits()) {
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
                if (approximateMatching_Unit(substring, getUnits())) {
                    processedString = processedString.replaceAll(substring, " ");
                    break;
                }
            }
        }

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
    private boolean approximateMatching_Unit(String substring, ArrayList<String> units) {
        for (String unit : units) {
            if (unit.length() > 2) {  //Exclude abbreviations/acronyms units
                if (StringUtils.getLevenshteinDistance(substring, unit, 1) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Initializes "units" from a file containing all subclasses of "concentration unit" (UO_0000051).
     *
     * @throws IOException
     */
    public void init() throws IOException {
        units = new ArrayList<>();
        InputStream bufferFile = this.getClass().getClassLoader().getResourceAsStream(
                "concentration_unit_dictionary.txt");

        if (bufferFile != null) {
            String stringFile = inputStream_To_String(bufferFile, 4000);
            if (stringFile != null && !stringFile.isEmpty()) {
                String[] lines = stringFile.split("\n");
                for (String line : lines) {
                    String[] fields = line.split("\t");
                    if (fields.length > 2) {
                        units.add(fields[0]);
                    }
                }
            }
        }
        getLog().info("Loaded units dictionary: obtained " + units.size() + " entries");
    }

    private String inputStream_To_String(InputStream is, int tam) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[tam];
        while (is.read(buffer, 0, tam) != -1) {
            sb.append(new String(buffer));
        }
        return sb.toString();
    }

    public ArrayList<String> getUnits() {
        return units;
    }
}