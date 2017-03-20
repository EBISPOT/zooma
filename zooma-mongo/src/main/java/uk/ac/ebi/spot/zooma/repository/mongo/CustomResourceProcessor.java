package uk.ac.ebi.spot.zooma.repository.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.mongo.Annotation;

import java.util.Collection;

/**
 * Created by olgavrou on 20/03/2017.
 */
@Component
public class CustomResourceProcessor implements ResourceProcessor<Resource<Annotation>>  {

    private RepositoryEntityLinks entityLinks;
    private AnnotationRepository annotationRepository;

    @Autowired
    public CustomResourceProcessor(RepositoryEntityLinks entityLinks, AnnotationRepository annotationRepository) {
        this.entityLinks = entityLinks;
        this.annotationRepository = annotationRepository;
    }


    @Override
    public Resource<Annotation> process(Resource<Annotation> resource) {
        Annotation annotation = annotationRepository.findOne(resource.getContent().getId());
        Collection<String> replacedBy = annotation.getReplacedBy();
        for(String r : replacedBy){
            resource.add(entityLinks.linkToSingleResource(AnnotationRepository.class,
                    r)
                    .withRel("replacedBy"));
        }
        String replaces = annotation.getReplaces();
        if(replaces != null && !replaces.isEmpty()){
            resource.add(entityLinks.linkToSingleResource(AnnotationRepository.class, replaces).withRel("replaces"));
        }
        return resource;
    }
}
