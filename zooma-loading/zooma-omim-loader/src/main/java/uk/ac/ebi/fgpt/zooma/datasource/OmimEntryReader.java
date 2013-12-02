package uk.ac.ebi.fgpt.zooma.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Parses omim.txt from a GZipped stream, reading out OMIM entry numbers, titles and alternative terms only.
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimEntryReader extends Reader {
    private final BufferedReader _reader;
    private int position = 0;

    private static final String RECORD_HEADER = "*RECORD*";
    private static final String ID_HEADER = "*FIELD* NO";
    private static final String TITLE_HEADER = "*FIELD* TI";
    private static final String LATER_HEADER = "*FIELD* ED";
    private static final String OTHER_HEADER = "*FIELD*";

    public OmimEntryReader(InputStream in) {
        this._reader = new BufferedReader(new InputStreamReader(in)) {
            @Override public String readLine() throws IOException {
                String line = super.readLine();
                position++;
                return line;
            }
        };
    }

    public OmimEntry readEntry() throws IOException {
        String idString = null;
        String titleString = null;
        List<String> altTitleStrings = null;

        String line;
        try {
            // read up to the next record entry
            while ((line = _reader.readLine()) != null) {
                if (line.trim().equals(RECORD_HEADER)) {
                    // start entry

                    try {
                        // read fields
                        while ((line = _reader.readLine()) != null) {
                            // found id?
                            if (line.trim().equals(ID_HEADER)) {
                                // read the immediate next line - this is the ID
                                if ((line = _reader.readLine()) != null) {
                                    idString = line.trim();
                                }
                            }

                            // found titles?
                            if (line.trim().equals(TITLE_HEADER)) {
                                // read the following lines until we hit a blank line or another title
                                StringBuilder titlesSB = new StringBuilder();
                                while ((line = _reader.readLine()) != null) {
                                    if (line.equals("\n") || line.startsWith(OTHER_HEADER)) {
                                        break;
                                    }
                                    else {
                                        titlesSB.append(line.trim());
                                    }
                                }

                                String titleLines = titlesSB.toString();
                                String[] titles = titleLines.split(";;");
                                if (titles.length > 0) {
                                    // remove the symbol + id, if present
                                    String titlePlusSymbol = titles[0];
                                    if (titlePlusSymbol.startsWith("^")) {
                                        // this term is deprecated, so skip (by reading the next entry
                                        return readEntry();
                                    }
                                    else {
                                        titleString = titlePlusSymbol.replaceFirst("([#%\\*\\+])?" + idString + "\\s", "");
                                    }
                                }
                                if (titles.length > 1) {
                                    altTitleStrings = Arrays.asList(titles).subList(1, titles.length);
                                }
                                else {
                                    altTitleStrings = Collections.emptyList();
                                }
                            }

                            // if we have reached a later field, break
                            if (line.trim().equals(LATER_HEADER)) {
                                break;
                            }
                        }
                    }
                    catch (IOException e) {
                        throw new IOException("I/O troubles whilst trying to read " + ID_HEADER, e);
                    }

                    // finish entry
                    break;
                }
            }
        }
        catch (IOException e) {
            throw new IOException("I/O troubles whilst trying to read " + RECORD_HEADER, e);
        }

        if (idString != null && titleString != null && altTitleStrings != null) {
            return new OmimEntry(idString, titleString, altTitleStrings);
        }
        else {
            if (line == null) {
                return null;
            }
            else {
                throw new RuntimeException("Failed to read enough fields for another record, " +
                                                   "but content still remains (line " + position + ")");
            }
        }
    }

    @Override public int read(char[] cbuf, int off, int len) throws IOException {
        return _reader.read(cbuf, off, len);
    }

    @Override public void close() throws IOException {
        _reader.close();
        position = 0;
    }
}
