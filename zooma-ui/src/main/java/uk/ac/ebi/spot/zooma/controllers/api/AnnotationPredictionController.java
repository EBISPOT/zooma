//package uk.ac.ebi.spot.zooma.controllers.api;
//
//import org.apache.commons.lang3.tuple.Pair;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.rest.webmvc.RepositoryLinksResource;
//import org.springframework.data.rest.webmvc.ResourceNotFoundException;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.data.web.PagedResourcesAssembler;
//import org.springframework.hateoas.ExposesResourceFor;
//import org.springframework.hateoas.PagedResources;
//import org.springframework.hateoas.ResourceProcessor;
//import org.springframework.hateoas.mvc.ControllerLinkBuilder;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import uk.ac.ebi.spot.zooma.model.SimpleAnnotationPrediction;
//import uk.ac.ebi.spot.zooma.model.api.Property;
//import uk.ac.ebi.spot.zooma.model.Property;
//import uk.ac.ebi.spot.zooma.model.UntypedProperty;
//import uk.ac.ebi.spot.zooma.service.AnnotationPredictionService;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by olgavrou on 03/01/2017.
// */
//@Controller
//@RequestMapping("/api/annotationsums")
//@ExposesResourceFor(SimpleAnnotationPrediction.class)
//public class AnnotationPredictionController implements ResourceProcessor<RepositoryLinksResource> {
//
//    @Autowired
//    AnnotationPredictionService annotationPredictionService;
//
//    @Override
//    public RepositoryLinksResource process(RepositoryLinksResource resource) {
//        resource.add(ControllerLinkBuilder.linkTo(AnnotationPredictionController.class).withRel("annotationsums"));
//        return resource;
//    }
//
//    @RequestMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
//    HttpEntity<PagedResources<SimpleAnnotationPrediction>> getAnnotationSums(
//            @RequestParam(value = "propertyValue") String propertyValue,
//            @RequestParam(value = "propertyType", required = false) String propertyType,
//            @RequestParam(value = "sources", required = false) String sources,
//            @PageableDefault(size = 20, page = 0) Pageable pageable,
//            PagedResourcesAssembler assembler
//    ) throws ResourceNotFoundException {
//        List<Property> propertyValues = new ArrayList<>();
//        if(propertyType != null) {
//            propertyValues.add(new Property(propertyType, propertyValue));
//        } else {
//            propertyValues.add(new UntypedProperty(propertyValue));
//        }
//
//        List<String> sourceList = new ArrayList<>();
//        if (sources != null) {
//            if (sources.contains(",")) {
//                for (String source : sources.split(",")) {
//                    sourceList.add(source);
//                }
//            } else {
//                sourceList.add(sources);
//            }
//        }
//
//        Map<Pair<String, String>, List<SimpleAnnotationPrediction>> summaryMap = annotationPredictionService.predict(propertyValues, sourceList);
//        List<SimpleAnnotationPrediction> list = summaryMap.values().iterator().next();
//        int start = pageable.getOffset();
//        int end = (start + pageable.getPageSize() > list.size() ? list.size() : (start + pageable.getPageSize()));
//        Page<SimpleAnnotationPrediction> document = new PageImpl<>(list.subList(start, end), pageable, list.size());
//        return new ResponseEntity<>( assembler.toResource(document), HttpStatus.OK);
//    }
//}
