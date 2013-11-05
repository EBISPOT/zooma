package uk.ac.ebi.fgpt.zooma.service;

import org.springframework.core.io.Resource;
import uk.ac.ebi.fgpt.zooma.util.AbstractDictionaryLoadingProcessor;

import java.util.Collections;
import java.util.List;

/**
 * This class handles the processing of properties of type "organism part".
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */
public class AnatomyProcessor extends AbstractDictionaryLoadingProcessor {
    // organism part qualifier dictionary contains all subclasses of "anatomical modifier" (EFO)
    // and all subclasses of "position" (PATO)
    public AnatomyProcessor(String dictionaryResourceName) {
        super(dictionaryResourceName);
    }

    public AnatomyProcessor(Resource dictionaryResource) {
        super(dictionaryResource);
    }

    @Override
    public float getBoostFactor() {
        return 0.9f;
    }

    /**
     * Returns true if the property type indicates this string is an organism part, or false otherwise.
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
     * Takes a string, processes it by removing any modifiers recognised from the dictionary, and returns the unmodified
     * form
     *
     * @param searchString the string to process
     * @return a form of the string stripped of all likely anatomy modifiers
     * @throws IllegalArgumentException
     */
    @Override
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        // lower case the entire string
        String processedString = searchString.toLowerCase();
        // can we find our search string in the dictionary?
        for (String organismPartQualifier : getDictionary()) {
            if (processedString.contains(" " + organismPartQualifier + " ") ||
                    processedString.startsWith(organismPartQualifier + " ") ||
                    processedString.endsWith(" " + organismPartQualifier)) {
                processedString = processedString.replaceAll(organismPartQualifier, " ");
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
}