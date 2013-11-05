package uk.ac.ebi.fgpt.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class handles the processing of properties of type "organism part".
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */
public class SearchStringProcessor_OrganismPart implements SearchStringProcessor {
    // organismPartQualifier contains all subclasses of "anatomical modifier" (EFO) and all subclasses of "position" (PATO)
    private ArrayList<String> organismPartQualifier;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override
    public float getBoostFactor() {
        return 0.9f;
    }

    /**
     * Returns true if the property type is equal to organism_part Returns false otherwise.
     */
    @Override
    public boolean canProcess(String searchString, String searchStringType) {
        if (searchStringType != null && !searchStringType.isEmpty()) {
            searchStringType = searchStringType.toLowerCase();
            if (searchStringType.contentEquals("organism_part") ||
                    searchStringType.contentEquals("organism part") ||
                    searchStringType.contentEquals("organismpart")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Takes a string, looks for qualifiers in the string, removes them and returns the processed string.
     */
    @Override
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        String processedString = searchString.toLowerCase();

        //Remove concentration unit
        for (String qualifier : getOrganismPartQualifier()) {
            if (processedString.contains(" " + qualifier + " ") ||
                    processedString.startsWith(qualifier + " ") ||
                    processedString.endsWith(" " + qualifier)) {
                processedString = processedString.replaceAll(qualifier, " ");
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
     * Initializes the array of qualifiers related to organism parts from two files: One file contains all subclasses of
     * "anatomical modifier" (EFO) The second one contains all subclasses of "position" (PATO)
     */
    public void init() throws IOException {
        organismPartQualifier = new ArrayList<>();
        InputStream bufferFile = this.getClass().getClassLoader().getResourceAsStream(
                "organism_part_qualifier_dictionary.txt");
        if (bufferFile != null) {
            String stringFile = inputStream_To_String(bufferFile, 1000);
            if (stringFile != null && !stringFile.isEmpty()) {
                String[] lines = stringFile.split("\n");
                for (String line : lines) {
                    String[] fields = line.split("\t");
                    if (fields.length > 2) {
                        organismPartQualifier.add(fields[0]);
                    }
                }
            }
        }
        getLog().info(
                "Loaded organism part qualifiers dictionary: obtained " + organismPartQualifier.size() + " entries");
    }


    private String inputStream_To_String(InputStream is, int tam) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[tam];
        while (is.read(buffer, 0, tam) != -1) {
            sb.append(new String(buffer));
        }
        return sb.toString();
    }

    public ArrayList<String> getOrganismPartQualifier() {
        return organismPartQualifier;
    }
}