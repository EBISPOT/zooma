package uk.ac.ebi.spot.zooma.service.neo4j;

import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.zooma.model.neo4j.*;
import uk.ac.ebi.spot.zooma.repository.neo4j.*;

import java.util.List;

/**
 * Created by olgavrou on 22/02/2017.
 */
@Service
public class AnnotationService {

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    Session session;


    @Transactional
    public void save(Annotation annotation){
        Annotation annotation1 = annotationRepository.findByMongoId(annotation.getMongoId());

        if(annotation1 == null) {
            session.save(annotation);
        }

    }

    @Transactional
    public void save(List<Annotation> annotations){
        annotationRepository.save(annotations);
    }

}
