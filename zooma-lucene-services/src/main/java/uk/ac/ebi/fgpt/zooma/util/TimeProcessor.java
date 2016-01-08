package uk.ac.ebi.fgpt.zooma.util;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles the processing of properties of type "time".
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */
public class TimeProcessor implements SearchStringProcessor {
    @Override
    public float getBoostFactor() {
        return 0.95f;
    }

    /**
     * Returns true if the search string contains any decimal or numeric values.
     */
    @Override
    public boolean canProcess(String searchString) {
        Pattern p = Pattern.compile(".*\\d.*");
        Matcher m = p.matcher(searchString);
        return m.find();
    }

    /**
     * Takes a string, looks for numbers (such as: int,floats and intervals) in the string, removes them and returns the
     * processed string.
     */
    @Override
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        String space = "\\s{0,2}";

        //pattern for number: int or float..
        String number_float = space + "\\d{1,10}\\.\\d{1,10}" + space;
        String number_int = space + "\\d{1,10}" + space;

        //pattern for interval (e.g: 3-4 days)..
        String interval_float =
                "((" + number_float + "-" + number_float + ")|(" + number_float + "to" + number_float + "))";
        String interval_int = "((" + number_int + "-" + number_int + ")|(" + number_int + "to" + number_int + "))";

        Pattern pattern_interval_float = Pattern.compile(interval_float);
        Pattern pattern_interval_int = Pattern.compile(interval_int);
        Pattern pattern_number_float = Pattern.compile(number_float);
        Pattern pattern_number_int = Pattern.compile(number_int);

        String matchedNumber;
        String processedString = searchString;

        // replace all matched numbers and intervals
        Matcher matcher_interval_float = pattern_interval_float.matcher(processedString);
        while (matcher_interval_float.find()) {
            matchedNumber = RegexUtils.escapeString(matcher_interval_float.group());
            processedString = processedString.replaceFirst(matchedNumber, "");
        }

        Matcher matcher_interval_int = pattern_interval_int.matcher(processedString);
        while (matcher_interval_int.find()) {
            matchedNumber = matcher_interval_int.group();
            processedString = processedString.replaceFirst(matchedNumber, "");
        }

        Matcher matcher_number_float = pattern_number_float.matcher(processedString);
        while (matcher_number_float.find()) {
            matchedNumber = matcher_number_float.group();
            processedString = processedString.replaceFirst(matchedNumber, "");
        }

        Matcher matcher_number_int = pattern_number_int.matcher(processedString);
        while (matcher_number_int.find()) {
            matchedNumber = matcher_number_int.group();
            processedString = processedString.replaceFirst(matchedNumber, "");
        }

        // finally, tidy up whitespace
        processedString = processedString.trim().replaceAll("\\s+", " ");

        if (!processedString.isEmpty()) {
            return Collections.singletonList(processedString);
        }
        else {
            return Collections.emptyList();
        }
    }
}