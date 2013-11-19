package uk.ac.ebi.fgpt.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.fgpt.zooma.Initializable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A {@link SearchStringProcessorProvider} that uses a property type mapping list, loaded from the classpath, to define
 * mappings between search string processors and property type filters.  In this way, only certain processors are used
 * to process queries for a particular property type
 *
 * @author Tony Burdett
 * @date 19/11/13
 */
public class PropertyTypeProcessorProvider extends Initializable implements SearchStringProcessorProvider {
    private final Resource mappingResource;

    private final Collection<SearchStringProcessor> processors;
    private Map<String, Collection<SearchStringProcessor>> filteredProcessors;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public PropertyTypeProcessorProvider(String mappingResourceName, Collection<SearchStringProcessor> processors) {
        this(new ClassPathResource(mappingResourceName), processors);
    }

    public PropertyTypeProcessorProvider(Resource mappingResource, Collection<SearchStringProcessor> processors) {
        this.mappingResource = mappingResource;
        this.processors = new HashSet<>();
        this.filteredProcessors = new HashMap<>();
        for (SearchStringProcessor processor : processors) {
            registerProcessor(processor);
        }
    }

    public Resource getMappingResource() {
        return mappingResource;
    }

    @Override public void registerProcessor(SearchStringProcessor processor) {
        processors.add(processor);
    }

    @Override public void registerFilteredProcessor(SearchStringProcessor processor, String filter) {
        // normalize string
        String normalizedFilter = normalizeFilterString(filter);
        if (!filteredProcessors.containsKey(normalizedFilter)) {
            filteredProcessors.put(normalizedFilter, new HashSet<SearchStringProcessor>());
        }
        filteredProcessors.get(normalizedFilter).add(processor);
    }

    @Override public Collection<SearchStringProcessor> getProcessors() {
        return processors;
    }

    @Override public Collection<SearchStringProcessor> getFilteredProcessors(String filter) {
        try {
            initOrWait();
            Collection<SearchStringProcessor> results = new HashSet<>();

            String normalizedFilter = normalizeFilterString(filter);
            results.addAll(filteredProcessors.get(normalizedFilter));
            results.addAll(processors);

            return results;
        }
        catch (InterruptedException e) {
            throw new RuntimeException(getClass().getSimpleName() + " was interrupted whilst initializing", e);
        }
    }

    @Override protected void doInitialization() throws Exception {
        // load mappings file from the classpath
        BufferedReader reader = new BufferedReader(new InputStreamReader(getMappingResource().getInputStream()));
        String line;
        int lineNumber = 1;
        while ((line = reader.readLine()) != null) {
            String entry = line.trim();
            if (!entry.startsWith("#")) {
                String[] fields = entry.split("\t+");
                if (fields.length == 2) {
                    String processorClassName = fields[0];
                    String mappingsString = fields[1];

                    SearchStringProcessor namedProcessor = null;
                    for (SearchStringProcessor processor : getProcessors()) {
                        if (processor.getClass().getName().equals(processorClassName)) {
                            namedProcessor = processor;
                            break;
                        }
                    }

                    if (namedProcessor != null) {
                        String[] filters = mappingsString.split(",");
                        for (String filter : filters) {
                            registerFilteredProcessor(namedProcessor, filter);
                        }
                    }
                    else {
                        throw new ParseException(
                                "Reading mapping file " + getMappingResource().getURL() + " failed - " +
                                        "no SearchStringProcessor named '" + processorClassName + "' " +
                                        "(line " + lineNumber + ") was registered with this provider", lineNumber);
                    }
                }
            }
            lineNumber++;
        }
        getLog().debug("Loaded processor mappings from '" + getMappingResource().getURL() + "'. " +
                               filteredProcessors.size() + " filtered processors were mapped");
    }

    @Override protected void doTermination() throws Exception {
    }

    private String normalizeFilterString(String filter) {
        // lowercase and remove all underscores and whitespace
        return filter.trim().toLowerCase().replace("_", "").replace("\\s", "");
    }
}
