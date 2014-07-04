package uk.ac.ebi.fgpt.zooma.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A reader implementation that is capable of reading a uniprot human disease plain text file into a series of {@link
 * uk.ac.ebi.fgpt.zooma.datasource.UniprotHumanDiseaseEntry} objects
 *
 * @author Tony Burdett
 * @date 04/07/14
 */
public class UniprotHumanDiseaseVocabReader extends Reader {
    private final BufferedReader _reader;
    private int position = 0;
    private boolean positionedAtRecord = false;

    private static final String CONTENT_START_REGEX = "_+";
    private static final String RECORD_HEADER = "//";
    private static final String NAME_HEADER = "ID   ";
    private static final String ACC_HEADER = "AC   ";
    private static final String SEMANTIC_TAG_HEADER = "DR   ";
    private static final String SYNONYM_HEADER = "SY   ";

    public UniprotHumanDiseaseVocabReader(InputStream in) {
        this._reader = new BufferedReader(new InputStreamReader(in)) {
            @Override public String readLine() throws IOException {
                String line = super.readLine();
                position++;
                return line;
            }
        };
    }

    public synchronized UniprotHumanDiseaseEntry readEntry() throws IOException {
        String accessionString = null;
        String nameString = null;
        String omimID = null;
        List<String> synonymStrings = null;

        String line = "";
        try {
            // read up to the next record entry
            do {
                if (positionedAtRecord) {
                    // start entry
                    synonymStrings = new ArrayList<>();

                    try {
                        // read fields
                        do {
                            // found name?
                            if (line.startsWith(NAME_HEADER)) {
                                nameString = line.replaceFirst(NAME_HEADER, "").trim();
                                if (nameString.endsWith(".")) {
                                    nameString = nameString.substring(0, nameString.length() - 1);
                                }
                            }

                            // found accession?
                            if (line.startsWith(ACC_HEADER)) {
                                accessionString = line.replaceFirst(ACC_HEADER, "").trim();
                                if (accessionString.endsWith(".")) {
                                    accessionString = accessionString.substring(0, accessionString.length() - 1);
                                }
                            }

                            // found semantic tag?
                            if (line.startsWith(SEMANTIC_TAG_HEADER)) {
                                String xrefString = line.replaceFirst(SEMANTIC_TAG_HEADER, "").trim();

                                // is this an OMIM ref?
                                String[] xrefTokens = xrefString.split(";", -1);
                                if (xrefTokens[0].trim().equals("MIM")) {
                                    omimID = xrefTokens[1].trim();
                                }
                            }

                            // found synonym?
                            if (line.startsWith(SYNONYM_HEADER)) {
                                String synonymString = line.replaceFirst(SYNONYM_HEADER, "").trim();
                                if (synonymString.endsWith(".")) {
                                    synonymString = synonymString.substring(0, synonymString.length() - 1);
                                }
                                synonymStrings.add(synonymString);
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
                        throw new IOException("I/O troubles whilst trying to read " + NAME_HEADER, e);
                    }

                    // finish entry
                    break;
                }
                else {
                    // not yet positioned at an entry - check next line
                    if (line.trim().equals(RECORD_HEADER) || line.matches(CONTENT_START_REGEX)) {
                        positionedAtRecord = true;
                    }
                }
            }
            while ((line = _reader.readLine()) != null);
        }
        catch (IOException e) {
            throw new IOException("I/O troubles whilst trying to read " + RECORD_HEADER, e);
        }

        if (accessionString != null && nameString != null) {
            return new UniprotHumanDiseaseEntry(accessionString, nameString, omimID, synonymStrings);
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

    @Override public synchronized int read(char[] cbuf, int off, int len) throws IOException {
        return _reader.read(cbuf, off, len);
    }

    @Override public synchronized void close() throws IOException {
        _reader.close();
        position = 0;
    }

}
