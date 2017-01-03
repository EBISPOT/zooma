package uk.ac.ebi.spot.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.services.MongoAnnotationSourceRepositoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by olgavrou on 24/11/2016.
 */
@Component
public class DataSources {

    @Autowired
    private MongoAnnotationSourceRepositoryService sourceRepositoryService;

    private Map<String, Map<String, String>> topicsToSourceNames;
    private List<String> results;

    public DataSources() {
    }

    public Map<String, Map<String, String>> getTopicsToSourceNames() {
        if (topicsToSourceNames == null){
            List<MongoAnnotationSource> sources = sourceRepositoryService.getAllDocuments();
            topicsToSourceNames = new HashMap<>();
            for(MongoAnnotationSource source : sources){
                Map<String, String> sourceList = topicsToSourceNames.get(source.getTopic());
                if (sourceList == null){
                    sourceList = new HashMap<>();
                }
                sourceList.put(source.getName(), "false");
                topicsToSourceNames.put(source.getTopic(), sourceList);
            }
        }
        return topicsToSourceNames;
    }

    public void setTopicsToSourceNames(Map<String, Map<String, String>> topicsToSourceNames) {
        this.topicsToSourceNames = topicsToSourceNames;
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }
}
