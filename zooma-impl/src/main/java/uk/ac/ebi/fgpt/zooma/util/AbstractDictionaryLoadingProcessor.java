package uk.ac.ebi.fgpt.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract implementation of a {@link SearchStringProcessor} that loads a dictionary of required terms at startup.
 * Dictionaries should be passed using Spring's {@link Resource} class, enabling loading from one of several types of
 * location.
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
     * Initializes this processor by loading the contents of the supplied dictionary resource into memory.  Once
     * initialized, the dictionary is available for use in downstream processing
     */
    public void init() throws IOException {
        this.dictionary = new HashSet<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(getDictionaryResource().getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            dictionary.add(line.trim());
        }
        getLog().debug("Loaded dictionary from '" + getDictionaryResource().getURL() + "'. " +
                               getDictionary().size() + " entries loaded");
    }
}
