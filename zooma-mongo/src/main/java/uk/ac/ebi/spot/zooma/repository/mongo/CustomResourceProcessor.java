package uk.ac.ebi.spot.zooma.repository.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
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

    @Value("${ols.term.location}")
    private String olsTermLocation;

    @Autowired
    public CustomResourceProcessor(RepositoryEntityLinks entityLinks) {
        this.entityLinks = entityLinks;
    }


    @Override
    public Resource<Annotation> process(Resource<Annotation> resource) {
        Collection<String> replacedBy = resource.getContent().getReplacedBy();
        for(String r : replacedBy){
            resource.add(entityLinks.linkToSingleResource(AnnotationRepository.class,
                    r)
                    .withRel("replacedBy"));
        }
        String replaces = resource.getContent().getReplaces();
        if(replaces != null && !replaces.isEmpty()){
            resource.add(entityLinks.linkToSingleResource(AnnotationRepository.class, replaces).withRel("replaces"));
        }

        Collection<String> semanticTags = resource.getContent().getSemanticTag();
        for (String semTag : semanticTags){
            Link link = new Link(olsTermLocation + semTag).withRel("olsLinks");
            resource.add(link);
        }

        return resource;
    }
}
