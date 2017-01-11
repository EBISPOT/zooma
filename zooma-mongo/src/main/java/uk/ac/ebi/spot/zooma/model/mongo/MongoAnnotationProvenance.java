package uk.ac.ebi.spot.zooma.model.mongo;


import lombok.Data;
import lombok.NonNull;
import uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance;

import java.util.Date;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Data public class MongoAnnotationProvenance implements AnnotationProvenance {

    @NonNull
    private DatabaseAnnotationSource source;
    @NonNull
    private Evidence evidence;
    @NonNull
    private Accuracy accuracy;
    @NonNull
    private String generator;

    private Date generatedDate;
    @NonNull
    private String annotator;
    @NonNull
    private Date annotationDate;

    public void setGeneratedDate(Date generatedDate) {
        if (generatedDate == null){
            this.generatedDate = new Date();
        } else {
            this.generatedDate = generatedDate;
        }
    }

    public Date getGeneratedDate(){
        if (this.generatedDate == null){
            this.generatedDate = new Date();
        }
        return this.generatedDate;
    }
}
