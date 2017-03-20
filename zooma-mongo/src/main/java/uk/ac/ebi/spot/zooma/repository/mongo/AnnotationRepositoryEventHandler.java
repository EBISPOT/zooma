package uk.ac.ebi.spot.zooma.repository.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.exception.AnnotationAlreadyExiststException;
import uk.ac.ebi.spot.zooma.messaging.Constants;
import uk.ac.ebi.spot.zooma.model.mongo.Annotation;
import uk.ac.ebi.spot.zooma.model.mongo.MongoAnnotationProvenance;
import uk.ac.ebi.spot.zooma.utils.MongoUtils;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    private final MessageDigest messageDigest = MongoUtils.generateMessageDigest();


    @Autowired
    public AnnotationRepositoryEventHandler(AmqpTemplate messagingTemplate, AnnotationRepository annotationRepository) {
        this.messagingTemplate = messagingTemplate;
        this.annotationRepository = annotationRepository;
    }

    @HandleAfterCreate
    public void handleAnnotationCreate(Annotation annotation) {
        getLog().info("New annotation created: " + annotation.getId());

        messagingTemplate.convertAndSend(Constants.Exchanges.ANNOTATION_FANOUT,"", annotation.toSimpleMap());
    }

    @HandleBeforeCreate
    public void handleAnnotationBeforeCreate(Annotation annotation){
        this.checkDuplicate(annotation);
        annotation.setChecksum(getAnnotationHash(annotation));
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
        this.checkDuplicate(annotation);

        //create the new annotation with the body of the sent annotation
        Annotation newAnnToSave = new Annotation(annotation.getBiologicalEntities(),
                annotation.getProperty(), annotation.getSemanticTag(),
                annotation.getProvenance(),
                "");
        newAnnToSave.setChecksum(getAnnotationHash(annotation));
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

    /**
     * Calculates the hash of the annotation and
     * throws an error if it already exists
     * @param annotation
     */
    private void checkDuplicate(Annotation annotation){
        String annHash = this.getAnnotationHash(annotation);
        Annotation existingAnn = annotationRepository.findByChecksum(annHash);
        if (existingAnn != null) {
            //update the annotation with nothing?
            throw new AnnotationAlreadyExiststException(existingAnn.getId());
        }
    }

    private String getAnnotationHash(Annotation annotation){
        List<String> idContents = new ArrayList<>();

        idContents.add(annotation.getBiologicalEntities().getBioEntity());
        idContents.add(annotation.getBiologicalEntities().getStudies().getStudy());
        idContents.add(annotation.getProperty().getPropertyType());
        idContents.add(annotation.getProperty().getPropertyValue());
        for (String st : annotation.getSemanticTag()){
            idContents.add(st);
        }

        MongoAnnotationProvenance provenance = annotation.getProvenance();
        idContents.add(provenance.getAnnotator() != null ? provenance.getAnnotator() : "");

        idContents.add(provenance.getAnnotatedDate() != null ? provenance.getAnnotatedDate().toString() : "");
        idContents.add(provenance.getEvidence() != null ? provenance.getEvidence().toString() : "");
        idContents.add(provenance.getSource().getUri() != null ? provenance.getSource().getUri() : "");

        return generateIDFromContent(idContents.toArray(new String[idContents.size()]));

    }

    private String generateIDFromContent(String... contents) {
        boolean hasNulls = false;
        for (String s : contents) {
            if (s == null) {
                hasNulls = true;
                break;
            }
        }
        if (hasNulls) {
            StringBuilder sb = new StringBuilder();
            for (String s : contents) {
                sb.append(s).append(";");
            }
            getLog().error("Attempting to generate new ID from content containing nulls: " + sb.toString());
        }
        synchronized (messageDigest) {
            return MongoUtils.generateHashEncodedID(messageDigest, contents);
        }
    }

}
