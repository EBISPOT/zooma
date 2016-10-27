package uk.ac.ebi.spot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.spot.model.AnnotationProperty;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.model.AnnotationSummaryRequest;
import uk.ac.ebi.spot.services.SolrAnnotationRepositoryService;

import java.util.*;

/**
 * Created by olgavrou on 25/10/2016.
 */
@RequestMapping("/spot/zooma")
@Controller
public class AnnotationSummaryController {

    @Autowired
    private SolrAnnotationRepositoryService solrAnnotationRepositoryService;

    @RequestMapping({"","/"})
    public String showZooma(Model model){
        model.addAttribute("annotationProperty", new AnnotationProperty());
        return "index";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String annotate(AnnotationProperty property, Model model){
        ArrayList<String> values = new ArrayList<>(Arrays.asList(property.getPropertyValue().split("\r\n")));

        Map<String, List<AnnotationSummaryRequest>> summaryMap = new HashMap<>();
        for (String value : values){
            List<AnnotationSummaryRequest> summaries = new ArrayList<>();

            List<AnnotationSummary> results = solrAnnotationRepositoryService.getByAnnotatedPropertyValueGroupBySemanticTags(value);

            for (AnnotationSummary summary : results){
                summaries.add(new AnnotationSummaryRequest("CONFIDENCE",
                        summary.getAnnotatedPropertyType(),
                        summary.getAnnotatedPropertyValue(),
                        summary.getSemanticTags(),
                        summary.getSource(),
                        summary.getQuality()));
            }
            summaryMap.put(value, summaries);
        }
        model.addAttribute("map", summaryMap);
        return "index";
    }

}
