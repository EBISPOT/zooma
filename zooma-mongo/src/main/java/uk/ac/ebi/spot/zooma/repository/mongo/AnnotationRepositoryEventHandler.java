package uk.ac.ebi.spot.zooma.repository.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.messaging.Constants;
import uk.ac.ebi.spot.zooma.model.mongo.Annotation;
import uk.ac.ebi.spot.zooma.utils.AnnotationChecksum;

import java.util.Collection;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/01/17
 */
@Service
@RepositoryEventHandler
public class AnnotationRepositoryEventHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    protected Logger getLog() {
        return log;
    }

    private AmqpTemplate messagingTemplate;

    private AnnotationRepository annotationRepository;

    private AnnotationChecksum annotationChecksum;

    @Autowired
    public AnnotationRepositoryEventHandler(AmqpTemplate messagingTemplate,
                                            AnnotationRepository annotationRepository,
                                            AnnotationChecksum annotationChecksum) {
        this.messagingTemplate = messagingTemplate;
        this.annotationRepository = annotationRepository;
        this.annotationChecksum = annotationChecksum;
    }

    @HandleAfterCreate
    public void handleAnnotationCreate(Annotation annotation) {
        getLog().info("New annotation created: " + annotation.getId());

        messagingTemplate.convertAndSend(Constants.Exchanges.ANNOTATION_FANOUT,"", annotation.toSimpleMap());
    }

    @HandleBeforeCreate
    public void handleAnnotationBeforeCreate(Annotation annotation){
        String hash = annotationChecksum.getChecksum(annotation, annotationRepository);
        annotation.setChecksum(hash);
        //if the user has set an id for the annotation
        //annotations should only be created by mongo
        annotation.setId(null);
    }

    /**
     * When we perform an update of an object in Zooma
     * we really want to create a new Document for the updated entities, have it point to the
     * old document through it's "replaces" field, and update the old Document's "replacedBy" field
     * to point to the new Document
     * @param annotation
     */
    @HandleBeforeSave
    public void handleAnnotationBeforeSave(Annotation annotation){
        String oldAnnotationId = annotation.getId();
        Annotation oldAnnotation = annotationRepository.findOne(oldAnnotationId);
        String checksum = annotationChecksum.getChecksum(annotation, annotationRepository);

        //create the new annotation with the body of the sent annotation
        Annotation newAnnToSave = new Annotation(annotation.getBiologicalEntities(),
                annotation.getProperty(), annotation.getSemanticTag(),
                annotation.getProvenance(),
                "");
        newAnnToSave.setChecksum(checksum);
        newAnnToSave.setReplaces(oldAnnotationId);
        Annotation newAnn = annotationRepository.insert(newAnnToSave);

        //replace the annotation to be "updated" with the old annotation (i.e it's elements are not updated)
        Collection<String> replacedBy = oldAnnotation.getReplacedBy();
        replacedBy.add(newAnn.getId());
        annotation.setReplacedBy(replacedBy);

        annotation.setId(oldAnnotationId);
        annotation.setSemanticTag(oldAnnotation.getSemanticTag());
        annotation.setReplaces(oldAnnotation.getReplaces());
        annotation.setChecksum(oldAnnotation.getChecksum());
        annotation.setBiologicalEntities(oldAnnotation.getBiologicalEntities());
        annotation.setProperty(oldAnnotation.getProperty());
        annotation.setProvenance(oldAnnotation.getProvenance());
        annotation.setQuality();

        getLog().info("Annotation: " + newAnn.getId() + " has replaced: " + annotation.getId());
    }
}
