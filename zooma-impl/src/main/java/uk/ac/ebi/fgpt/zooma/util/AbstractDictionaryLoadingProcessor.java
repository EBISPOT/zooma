package uk.ac.ebi.fgpt.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract implementation of a {@link SearchStringProcessor} that loads a dictionary of required terms at startup.
 * Dictionaries should be passed using Spring's {@link Resource} class, enabling loading from one of several types of
 * location.
 * <p/>
 * Dictionaries have a simple defined format.  They should contain one dictionary term per line.  Each line consists of
 * three elements: first, the term; second, the ontology entry that defines this term; third, a boolean flag indicating
 * whether this term exactly matches the label in the ontology.  Only the first element is required.  Elements are tab
 * separated.
 *
 * @author Tony Burdett
 * @date 05/11/13
 */
public abstract class AbstractDictionaryLoadingProcessor implements SearchStringProcessor {
    private final Resource dictionaryResource;
    private Set<String> dictionary;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public AbstractDictionaryLoadingProcessor(String dictionaryResourceName) {
        this(new ClassPathResource(dictionaryResourceName));
    }

    public AbstractDictionaryLoadingProcessor(Resource dictionaryResource) {
        this.dictionaryResource = dictionaryResource;
    }

    public Resource getDictionaryResource() {
        return dictionaryResource;
    }

    public Set<String> getDictionary() {
        return dictionary;
    }

    /**
     * Returns true if the search string contains any terms loaded from the dictionary.  This is a very basic
     * implementation of this method based on the loaded dictionary, and you may wish to override this to be smarter.
     * Subsequent processing may indicate that these modifiers are not eligible to be removed - this is simply a
     * pre-screen.
     */
    @Override
    public boolean canProcess(String searchString) {
        for (String term : getDictionary()) {
            if (searchString.contains(term)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes this processor by loading the contents of the supplied dictionary resource into memory.  Once
     * initialized, the dictionary is available for use in downstream processing
     */
    public void init() throws IOException {
        this.dictionary = new HashSet<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(getDictionaryResource().getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
                String entry = line.trim();
            if (!entry.startsWith("#")) {
                String[] fields = entry.split("\t+");
                if (fields.length > 0) {
                    dictionary.add(fields[0]);
                }
            }
        }
        getLog().debug("Loaded dictionary from '" + getDictionaryResource().getURL() + "'. " +
                               getDictionary().size() + " entries loaded");
    }
}
