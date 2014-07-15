package uk.ac.ebi.fgpt.zooma.util;

/**
 * A utils class that allows the replacement of a 'literal' string with the regex-safe equivalent.  In other words,
 * takes a string and replaces any regex control character with it's escaped equivalent.
 *
 * @author Tony Burdett
 * @date 14/07/14
 */
public class RegexUtils {
    private static final String controlCharacters = "[\\^$.|?*+()";

    /**
     * Escapes any control characters within the string, 's', replacing them with their escaped form.
     *
     * @param s the string to escape
     * @return the escaped form of the supplied string
     */
    public static String escapeString(String s) {
        for (int i = 1; i <= controlCharacters.length(); i++) {
            CharSequence cs = controlCharacters.subSequence(i - 1, i);
            if (s.contains(cs)) {
                s = s.replace(cs, "\\" + cs.charAt(0));
            }
        }
        return s;
    }
}
