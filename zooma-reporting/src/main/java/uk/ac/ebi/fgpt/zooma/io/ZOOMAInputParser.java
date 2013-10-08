package uk.ac.ebi.fgpt.zooma.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.SimpleUntypedProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple parser for ZOOMA-format tab delimited text files.  Input files should contain a list of properties to
 * annotate from ZOOMA.
 * <p/>
 * The input format is simple: one property per line, property value is the first cell, tab, then (optionally) the
 * property type the second cell.  If there is no property type all annotations for the given property value will be
 * searched.  If there is a property type, results will be constrained to ONLY those which match the type.  Property
 * types are case-insensitive, and whitespace or underscores are removed.
 *
 * @author Tony Burdett
 * @date 03/09/12
 */
public class ZOOMAInputParser {
    private InputStream in;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZOOMAInputParser(URL inputLocation) throws IOException {
        this(inputLocation.openStream());
    }

    public ZOOMAInputParser(String inputFileName) throws FileNotFoundException {
        this(new File(inputFileName));
    }

    public ZOOMAInputParser(File inputFile) throws FileNotFoundException {
        this(new FileInputStream(inputFile));
    }

    public ZOOMAInputParser(InputStream in) {
        this.in = in;
    }

    /**
     * Parses properties from the supplied input and returns the list (in the original order from the file).
     *
     * @return a list of properties read from the input source
     */
    public List<Property> parse() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        List<Property> results = new ArrayList<>();

        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;

            // tokenise this line
            String[] elements = splitLine(line);

            // handle elements
            switch (elements.length) {
                case 1:
                    results.add(new SimpleUntypedProperty(elements[0]));
                    break;
                case 2:
                    results.add(new SimpleTypedProperty(elements[1], elements[0]));
                    break;
                case 0:
                    getLog().debug("Empty line in input file detected, skipping");
                    break;
                default:
                    getLog().error("Line " + lineNumber + " could not be read - too many elements [" + line + "]");
            }
        }
        getLog().info("Read " + results.size() + " properties from " + lineNumber + " lines");
        return results;
    }

    /**
     * Releases any resources held by this parser.
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * Splits this line into appropriate cells in accordance with the MAGE-TAB specification.  Empty cells are preserved
     * as empty strings.  This is equivalent to calling <code>splitLine(line, false);</code> (see {@link
     * #splitLine(String, boolean)}).
     *
     * @param line the line to split
     * @return the resulting string array
     */
    public static String[] splitLine(String line) {
        return splitLine(line, false);
    }

    /**
     * Splits this line into appropriate cells in accordance with the MAGE-TAB specification.  If escaping is present
     * around any strings on this line, the "logical" cells will be different from the "physical" cells - any quoted tab
     * characters will not be considered to be cell delimiters (i.e. some cells may contain tab characters) - unless
     * ignoreEscaping is set to true. Empty cells are preserved as empty strings.  This is equivalent to calling
     * <code>splitLine(line, ignoreEscaping, true);</code> (see {@link #splitLine(String, boolean, boolean)}).
     *
     * @param line           the line to split
     * @param ignoreEscaping whether to ignore escaped tab and newline characters or whether to handle them according to
     *                       the MAGE-TAB spec.
     * @return the resulting string array
     */
    public static String[] splitLine(String line, boolean ignoreEscaping) {
        return splitLine(line, ignoreEscaping, true);
    }

    /**
     * Splits this line into appropriate cells in accordance with the MAGE-TAB specification, ingoring any escaping
     * found.  Empty cells are preserved as empty strings.  Any trailing whitespace in cells present is trimmed if
     * 'trimWhitespace' is true, otherwise every cell is preserved verbosely.
     *
     * @param line           the line to split
     * @param ignoreEscaping whether to ignore escaped tab and newline characters or whether to handle them according to
     *                       the MAGE-TAB spec.
     * @param trimWhitespace whether to act in 'strict' mode, preserving any leading and trailing whitespace characters,
     *                       or whether to automatically remove them
     * @return the resulting string array
     */
    public static String[] splitLine(String line, boolean ignoreEscaping, boolean trimWhitespace) {
        if (!ignoreEscaping) {
            String[] cells;
            if (trimWhitespace) {
                // trim the line and split into cells
                cells = line.trim().split("\t", -1);
            }
            else {
                // just split into cells
                cells = line.split("\t", -1);
            }

            List<String> logicalCells = new ArrayList<String>();

            StringBuffer sb = null;
            for (String s : cells) {
                // trim if desired
                String cell = s;
                if (trimWhitespace) {
                    cell = cell.trim();
                }

                if (cell.startsWith("\"") && !cell.endsWith("\"")) {
                    // this cell starts with a quote but doesn't end with one
                    // so this is the start of a new logical cell
                    // NOTE that we might have trimmed escaped whitespace from the end of this string, so restore

                    // restore by adding a full stop to the end of the original string, then trim
                    cell = s.concat(".").trim();
                    // now remove final character
                    cell = cell.substring(0, cell.length() - 1);
                    // start new logical cell
                    sb = new StringBuffer();
                    sb.append(cell).append("\t");
                }
                else {
                    if (!cell.startsWith("\"") && cell.endsWith("\"")) {
                        // this cell ends with a quote but doesn't start with one
                        // so this is could be a continuation of the current logical cell
                        if (sb != null) {
                            // there is a logical cell, append
                            sb.append(cell);
                            String logicalCell = sb.toString();
                            logicalCells.add(logicalCell);
                        }
                        else {
                            // there is no current logical cell, so just ignore...
                            // might just be that the cell contains a quoted remark (e.g. 'Tony said "Hello World"')
                            logicalCells.add(cell);
                        }
                    }
                    else {
                        // this cell both starts and ends with or without quotes
                        logicalCells.add(cell);
                    }
                }
            }
            return logicalCells.toArray(new String[logicalCells.size()]);
        }
        else {
            // trim if desired - check each cell
            if (trimWhitespace) {
                // trim the line and split into cells
                String[] cells = line.trim().split("\t", -1);
                List<String> logicalCells = new ArrayList<String>();
                for (String cell : cells) {
                    logicalCells.add(cell.trim());
                }
                return logicalCells.toArray(new String[logicalCells.size()]);
            }
            else {
                return line.split("\t", -1);
            }
        }
    }
}
