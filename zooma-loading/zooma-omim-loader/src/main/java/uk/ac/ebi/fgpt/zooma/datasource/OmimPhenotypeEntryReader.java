package uk.ac.ebi.fgpt.zooma.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Parses omim.txt from a GZipped stream, reading out OMIM entry numbers, titles and alternative terms only.
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimPhenotypeEntryReader extends Reader {
    private final BufferedReader _reader;
    private int position = 0;
    private boolean positionedAtRecord = false;

    private static final String RECORD_HEADER = "*RECORD*";
    private static final String ID_HEADER = "*FIELD* NO";
    private static final String TITLE_HEADER = "*FIELD* TI";
    private static final String CREATOR_HEADER = "*FIELD* CD";
    private static final String CONTRIBUTOR_HEADER = "*FIELD* CN";
    private static final String OTHER_HEADER = "*FIELD*";

    public OmimPhenotypeEntryReader(InputStream in) {
        this._reader = new BufferedReader(new InputStreamReader(in)) {
            @Override public String readLine() throws IOException {
                String line = super.readLine();
                position++;
                return line;
            }
        };
    }

    public OmimPhenotypeEntry readEntry() throws IOException {
        String idString = null;
        String titleString = null;
        List<String> altTitleStrings = null;

        String annotator = null;
        Date annotationDate = null;

        boolean hasContributor = false;

        String line = "";
        try {
            // read up to the next record entry
            do {
                if (positionedAtRecord) {
                    // start entry
                    try {
                        // read fields
                        do {
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
                                    if (titlePlusSymbol.matches("^[#\\^\\*\\+].*")) {
                                        // this term is not a phenotype, so recurse to next entry
                                        return readEntry();
                                    }
                                    else {
                                        titleString = titlePlusSymbol.replaceFirst("%?" + idString + "\\s", "");
                                    }
                                }
                                if (titles.length > 1) {
                                    altTitleStrings = Arrays.asList(titles).subList(1, titles.length);
                                }
                                else {
                                    altTitleStrings = Collections.emptyList();
                                }
                            }

                            // found creator?
                            if (line.trim().equals(CREATOR_HEADER) && !hasContributor) {
                                if ((line = _reader.readLine()) != null) {
                                    String annotatorInfo = line.replaceFirst(":\\s", ";");
                                    String[] tokens = annotatorInfo.split(";");
                                    annotator = tokens[0];
                                    if (tokens.length > 1) {
                                        try {
                                            annotationDate = new SimpleDateFormat("MM/dd/yyyy").parse(tokens[1]);
                                        }
                                        catch (ParseException e) {
                                            throw new RuntimeException(
                                                    "Failed to parse date '" + tokens[1] + "' " +
                                                            "in contributor field (line " + position + ")");
                                        }
                                    }
                                }
                            }

                            // found contributor?
                            if (line.trim().equals(CONTRIBUTOR_HEADER)) {
                                hasContributor = true;
                                if ((line = _reader.readLine()) != null) {
                                    String annotatorInfo = line.replaceFirst("\\s\\-\\s\\w+:\\s", ";");
                                    annotatorInfo = annotatorInfo.replaceFirst(":\\s", ";");
                                    String[] tokens = annotatorInfo.split(";");
                                    annotator = tokens[0];
                                    if (tokens.length > 1) {
                                        try {
                                            annotationDate = new SimpleDateFormat("MM/dd/yyyy").parse(tokens[1]);
                                        }
                                        catch (ParseException e) {
                                            throw new RuntimeException(
                                                    "Failed to parse date '" + tokens[1] + "' " +
                                                            "in contributor field (line " + position + ")");
                                        }
                                    }
                                }
                            }

                            if (!positionedAtRecord) {
                                // if we have reached the next record, break
                                if (line.trim().equals(RECORD_HEADER)) {
                                    positionedAtRecord = true;
                                    break;
                                }
                            }
                            else {
                                // reset positioned at record flag ready to read the next line
                                positionedAtRecord = false;
                            }
                        }
                        while ((line = _reader.readLine()) != null);
                    }
                    catch (IOException e) {
                        throw new IOException("I/O troubles whilst trying to read " + ID_HEADER, e);
                    }

                    // finish entry
                    break;
                }
                else {
                    // not yet positioned at an entry - check next line
                    if (line.trim().equals(RECORD_HEADER)) {
                        positionedAtRecord = true;
                    }
                }
            }
            while ((line = _reader.readLine()) != null);
        }
        catch (IOException e) {
            throw new IOException("I/O troubles whilst trying to read " + RECORD_HEADER, e);
        }

        if (idString != null && titleString != null && altTitleStrings != null) {
            return new OmimPhenotypeEntry(idString, titleString, altTitleStrings, annotator, annotationDate);
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
