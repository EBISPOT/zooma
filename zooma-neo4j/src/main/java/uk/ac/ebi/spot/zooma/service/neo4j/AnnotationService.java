package uk.ac.ebi.spot.zooma.service.neo4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.zooma.model.neo4j.Annotation;
import uk.ac.ebi.spot.zooma.repository.neo4j.AnnotationRepository;

import java.util.List;

/**
 * Created by olgavrou on 22/02/2017.
 */
@Service
public class AnnotationService {

    @Autowired
    private AnnotationRepository annotationRepository;

    @Transactional
    public Annotation save(Annotation annotation){
        Annotation savedAnn = annotationRepository.save(annotation);
        return savedAnn;
    }

    @Transactional
    public void save(List<Annotation> annotations){
        annotationRepository.save(annotations);
    }

}
