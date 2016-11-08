package uk.ac.ebi.spot.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.spot.model.AnnotationProperty;
import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.model.SimpleAnnotationPrediction;
import uk.ac.ebi.spot.services.AnnotationPredictionService;

import java.util.*;

/**
 * Created by olgavrou on 25/10/2016.
 */
@RequestMapping("/spot/zooma")
@Controller
public class AnnotationPredictionController {

    @Autowired
    AnnotationPredictionService annotationPredictionService;


    @RequestMapping({"","/"})
    public String showZooma(Model model){
        model.addAttribute("annotationProperty", new AnnotationProperty());
        return "index";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String annotate(AnnotationProperty property, Model model){

        List<Property> properties = property.getProperties();

        Map<Pair<String, String>, List<SimpleAnnotationPrediction>> summaryMap = annotationPredictionService.predict(properties);

        model.addAttribute("map", summaryMap);
        return "index";
    }

}
