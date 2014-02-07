package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSourceService;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Simon Jupp
 * @date 06/02/2014
 * Functional Genomics Group EMBL-EBI
 */
public abstract class SourceFilteredSuggestEndpoint<T, I> extends SuggestEndpoint<T, I> {

    private AnnotationSourceService annotationSourceService;

    public AnnotationSourceService getAnnotationSourceService() {
        return annotationSourceService;
    }

    @Autowired
    public void setAnnotationSourceService(AnnotationSourceService annotationSourceService) {
        this.annotationSourceService = annotationSourceService;
    }


    protected SearchType validateFilterArguments(String filter) {
        // filter argument should look like this:
        // &filter=required:[x,y];preferred:[x,y,z]
        // where required or preferred are optional and both arrays can contain any number of elements
        if (filter.contains("required")) {
            if (filter.contains("preferred")) {
                return SearchType.REQUIRED_AND_PREFERRED;
            }
            else {
                return SearchType.REQUIRED_ONLY;
            }
        }
        else {
            if (filter.contains("preferred")) {
                return SearchType.PREFERRED_ONLY;
            }
            else {
                return SearchType.UNRESTRICTED;
            }
        }
    }

    protected URI[] parseRequiredSourcesFromFilter(String filter) {
        Matcher requiredMatcher = Pattern.compile("required:\\[([^\\]]+)\\]").matcher(filter);
        List<URI> requiredSources = new ArrayList<>();
        if (requiredMatcher.find(filter.indexOf("required:"))) {
            String sourceNames = requiredMatcher.group(1);
            String[] tokens = sourceNames.split(",", -1);
            for (String sourceName : tokens) {
                AnnotationSource nextSource = getAnnotationSourceService().getAnnotationSource(sourceName);
                if (nextSource != null) {
                    requiredSources.add(nextSource.getURI());
                }
                else {
                    getLog().warn("Required source '" + sourceName + "' was specified as a filter but " +
                            "could not be found in ZOOMA; this source will be excluded from the query");
                }
            }
        }

        return requiredSources.toArray(new URI[requiredSources.size()]);
    }

    protected List<URI> parsePreferredSourcesFromFilter(String filter) {
        Matcher requiredMatcher = Pattern.compile("preferred:\\[([^\\]]+)\\]").matcher(filter);
        List<URI> preferredSources = new ArrayList<>();
        if (requiredMatcher.find(filter.indexOf("preferred:"))) {
            String sourceNames = requiredMatcher.group(1);
            String[] tokens = sourceNames.split(",", -1);
            for (String sourceName : tokens) {
                AnnotationSource nextSource = getAnnotationSourceService().getAnnotationSource(sourceName);
                if (nextSource != null) {
                    preferredSources.add(nextSource.getURI());
                }
                else {
                    getLog().warn("Required source '" + sourceName + "' was specified as a filter but " +
                            "could not be found in ZOOMA; this source will be excluded from the query");
                }
            }
        }
        return preferredSources;
    }

    protected enum SearchType {
        REQUIRED_ONLY,
        PREFERRED_ONLY,
        REQUIRED_AND_PREFERRED,
        UNRESTRICTED
    }
}
