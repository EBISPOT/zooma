package uk.ac.ebi.fgpt.zooma.util;

import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.List;

/**
 * This class handles the processing of properties of type "organism part".
 *
 * @author Jose Iglesias
 * @date 16/08/13
 */
public class AnatomyProcessor extends AbstractDictionaryLoadingProcessor {
    // should only be invoked if type is some derivation of 'organism part'

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