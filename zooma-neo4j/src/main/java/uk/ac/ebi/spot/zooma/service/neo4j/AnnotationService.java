package uk.ac.ebi.spot.zooma.service.neo4j;

import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.zooma.model.neo4j.*;
import uk.ac.ebi.spot.zooma.repository.neo4j.*;

import java.util.List;

/**
 * Service to save the Neo Annotations or other parts of the graph
 * Created by olgavrou on 22/02/2017.
 */
@Service
public class  AnnotationService {


    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    ProvenanceRepository provenanceRepository;


    @Transactional
    public void save(Provenance provenance){
        Provenance p = provenanceRepository.save(provenance);
        getLog().info("Saved Provenance Relationship: Annotation (id:  " + p.getAnnotation().getId() + ")");
    }

    @Transactional
    public void save(List<Annotation> annotations){
        annotationRepository.save(annotations);
    }

    @Transactional
    public Annotation findByMongoid(String mongoid){
        return annotationRepository.findAnnotationByMongoid(mongoid);
    }

}
