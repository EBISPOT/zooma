package uk.ac.ebi.spot.zooma.model.mongo;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.ac.ebi.spot.zooma.model.api.MongoDocument;

import java.net.URI;
import java.util.Collection;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Document(collection = "annotations")
@Data
public class Annotation implements MongoDocument {

    @Id
    private String id;
    @NonNull
    private Collection<BiologicalEntity> biologicalEntities;
    @NonNull
    private TypedProperty property;
    @NonNull
    private Collection<String> semanticTag;
    @NonNull
    private MongoAnnotationProvenance provenance;
    @NonNull
    private boolean batchLoad;
    private Float quality;


    public float getQuality() {
        if (this.quality == null){
            this.quality = calculateAnnotationQuality();
        }
        return quality;
    }

    /**
     * Quality can not be set manually.
     * It can only calculated based on the annotation's {@link uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance}
     */
    public void setQuality(){
        this.quality = calculateAnnotationQuality();
    }

    /**
     * Returns a float value that is the quality score for the given annotation.
     * <p/>
     * This score is evaluated by an algorithm that considers: <ul> <li>Source (e.g. Atlas, AE2, ZOOMA)</li>
     * <li>Evidence (Manually created, Inferred, etc.)</li> <li>Creator - Who made this annotation?</li> <li>Time of
     * creation - How recent is this annotation?</li> </ul>
     */
    private float calculateAnnotationQuality() throws IllegalArgumentException {

        if (this.provenance == null){
            throw new IllegalArgumentException("Provenance isn't set yet, can not calculate annotation provenance");
        }
        // evidence is most important factor, invert so ordinal 0 gets highest score
        int evidenceScore = MongoAnnotationProvenance.Evidence.values().length - this.provenance.getEvidence().ordinal();
        // creation time should then work backwards from most recent to oldest
        long age = this.provenance.getAnnotationDate().getTime();

        return (float) (evidenceScore + Math.log10(age));

    }
}
