package uk.ac.ebi.fgpt.zooma.search;

import org.apache.log4j.Logger;

/**
 * @author http://itssmee.wordpress.com/2010/06/28/java-example-of-damerau-levenshtein-distance/
 * Similar to Levenshtein, Damerau-Levenshtein also calculates the edit distances between two strings.
 * It based around comparing two strings and counting the number of insertions, deletions, and
 * substitution of single characters, and transposition of two characters.
 */
public class DamerauLevenshtein {
    private String compOne;
    private String compTwo;
    private int[][] matrix;
    private Boolean calculated = false;
    public static final Logger log = Logger.getLogger(DamerauLevenshtein.class);


    public DamerauLevenshtein(String a, String b) {
        if ((a.length() > 0 || !a.isEmpty()) || (b.length() > 0 || !b.isEmpty())) {
            compOne = a;
            compTwo = b;
        }
    }

    public int[][] getMatrix() {
        setupMatrix();
        return matrix;
    }

    public int getNumDiffs() {
        if (!calculated) setupMatrix();
        return matrix[compOne.length()][compTwo.length()];
    }

    public float getPctDiff(){
        float diffs = getNumDiffs();
        return (diffs/Math.min(compOne.length(),compTwo.length()));
    }

    private void setupMatrix() {
        int cost = -1;
        int del, sub, ins;

        matrix = new int[compOne.length() + 1][compTwo.length() + 1];

        for (int i = 0; i <= compOne.length(); i++) {
            matrix[i][0] = i;
        }

        for (int i = 0; i <= compTwo.length(); i++) {
            matrix[0][i] = i;
        }

        for (int i = 1; i <= compOne.length(); i++) {
            for (int j = 1; j <= compTwo.length(); j++) {
                if (compOne.charAt(i - 1) == compTwo.charAt(j - 1)) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                del = matrix[i - 1][j] + 1;
                ins = matrix[i][j - 1] + 1;
                sub = matrix[i - 1][j - 1] + cost;

                matrix[i][j] = minimum(del, ins, sub);

                if ((i > 1) && (j > 1) && (compOne.charAt(i - 1) == compTwo.charAt(j - 2)) && (compOne.charAt(i - 2) == compTwo.charAt(j - 1))) {
                    matrix[i][j] = minimum(matrix[i][j], matrix[i - 2][j - 2] + cost);
                }
            }
        }

        calculated = true;
        displayMatrix();
    }

    private void displayMatrix() {
//        log.debug("  " + compOne);
        for (int y = 0; y <= compTwo.length(); y++) {
//            if (y - 1 < 0) log.debug(" ");
//            else log.debug(compTwo.charAt(y - 1));
            for (int x = 0; x <= compOne.length(); x++) {
//               log.debug(matrix[x][y]);
            }
//            log.debug("");
        }
    }

    private int minimum(int d, int i, int s) {
        int m = Integer.MAX_VALUE;

        if (d < m) m = d;
        if (i < m) m = i;
        if (s < m) m = s;

        return m;
    }

    private int minimum(int d, int t) {
        int m = Integer.MAX_VALUE;

        if (d < m) m = d;
        if (t < m) m = t;

        return m;
    }

    public int getDHSimilarity() {
        int res = -1;
        int INF = compOne.length() + compTwo.length();

        matrix = new int[compOne.length() + 1][compTwo.length() + 1];

        for (int i = 0; i < compOne.length(); i++) {
            matrix[i + 1][1] = i;
            matrix[i + 1][0] = INF;
        }

        for (int i = 0; i < compTwo.length(); i++) {
            matrix[1][i + 1] = i;
            matrix[0][i + 1] = INF;
        }

        int[] DA = new int[24];

        for (int i = 0; i < 24; i++) {
            DA[i] = 0;
        }

        for (int i = 1; i < compOne.length(); i++) {
            int db = 0;

            for (int j = 1; j < compTwo.length(); j++) {

                int i1 = DA[compTwo.indexOf(compTwo.charAt(j - 1))];
                int j1 = db;
                int d = ((compOne.charAt(i - 1) == compTwo.charAt(j - 1)) ? 0 : 1);
                if (d == 0) db = j;

                matrix[i + 1][j + 1] = Math.min(Math.min(matrix[i][j] + d, matrix[i + 1][j] + 1), Math.min(matrix[i][j + 1] + 1, matrix[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1)));
            }
            DA[compOne.indexOf(compOne.charAt(i - 1))] = i;
        }

        return matrix[compOne.length()][compTwo.length()];
    }
}
