//package uk.ac.ebi.spot.zooma.controllers.ui;
//
//import org.apache.commons.lang3.tuple.Pair;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import uk.ac.ebi.spot.zooma.model.AnnotationProperty;
//import uk.ac.ebi.spot.zooma.model.SimpleAnnotationPrediction;
//import uk.ac.ebi.spot.zooma.model.api.Property;
//import uk.ac.ebi.spot.zooma.service.AnnotationPredictionService;
//
//import java.util.*;
//
///**
// * Created by olgavrou on 25/10/2016.
// */
//@RequestMapping("/spot/zooma")
//@Controller
//public class AnnotationPredictionControllerUI {
//
//    @Autowired
//    AnnotationPredictionService annotationPredictionService;
//
////    @Autowired
////    MongoAnnotationSourceRepositoryService annotationSourceRepositoryService;
////
////    @Autowired
////    DataSources datasources;
//
//    @RequestMapping({"","/"})
//    public String showZooma(Model model){
//        model.addAttribute("annotationProperty", new AnnotationProperty());
//        return "index";
//    }
//
//    @RequestMapping(method = RequestMethod.POST)
//    public String annotate(AnnotationProperty property, Model model){
//
//        List<Property> properties = property.getProperties();
//        Map<Pair<String, String>, List<SimpleAnnotationPrediction>> summaryMap = annotationPredictionService.predict(properties, null);
//
//        model.addAttribute("map", summaryMap);
//        return "index";
//    }
//
//}
