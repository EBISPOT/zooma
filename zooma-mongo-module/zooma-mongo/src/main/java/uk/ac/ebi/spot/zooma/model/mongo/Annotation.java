package uk.ac.ebi.spot.zooma.model.mongo;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.ac.ebi.spot.zooma.model.BaseAnnotation;
import uk.ac.ebi.spot.zooma.model.BiologicalEntity;
import uk.ac.ebi.spot.zooma.model.MongoAnnotationProvenance;
import uk.ac.ebi.spot.zooma.model.TypedProperty;
import uk.ac.ebi.spot.zooma.model.api.MongoDocument;

import java.util.Collection;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Document(collection = "annotations")
@Data
public class Annotation extends BaseAnnotation implements MongoDocument {

    @Id
    @CreatedBy
    private String id;

    public Annotation(){
        super();
    }

    public Annotation(BiologicalEntity biologicalEntities, TypedProperty property, Collection<String> semanticTag, MongoAnnotationProvenance provenance, boolean batchLoad) {
        super(biologicalEntities, property, semanticTag, provenance, batchLoad);
    }

}
