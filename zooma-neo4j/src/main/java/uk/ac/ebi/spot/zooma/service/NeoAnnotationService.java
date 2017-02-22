package uk.ac.ebi.spot.zooma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.zooma.model.Annotation;
import uk.ac.ebi.spot.zooma.repository.NeoAnnotationRepository;

import java.util.List;

/**
 * Created by olgavrou on 22/02/2017.
 */
@Service
public class NeoAnnotationService {

    @Autowired
    private NeoAnnotationRepository annotationRepository;

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
