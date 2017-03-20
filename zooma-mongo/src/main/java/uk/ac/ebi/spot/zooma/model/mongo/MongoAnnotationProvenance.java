package uk.ac.ebi.spot.zooma.model.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Data
@NoArgsConstructor
public class MongoAnnotationProvenance implements AnnotationProvenance {

    @NonNull
    private DatabaseAnnotationSource source;
    @NonNull
    private Evidence evidence;
    @NonNull
    private Accuracy accuracy;
    @NonNull
    private String generator;

    private LocalDateTime generatedDate;
    @NonNull
    private String annotator;
    @NonNull
    private LocalDateTime annotatedDate;

    public MongoAnnotationProvenance(DatabaseAnnotationSource source, Evidence evidence,
                                     Accuracy accuracy, String generator, String annotator, LocalDateTime annotatedDate){
        this.source = source;
        this.evidence = evidence;
        this.accuracy = accuracy;
        this.generator = generator;
        this.annotator = annotator;
        this.annotatedDate = annotatedDate;
        setGeneratedDate(LocalDateTime.now());
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        if (generatedDate == null){
            this.generatedDate = LocalDateTime.now();
        } else {
            this.generatedDate = generatedDate;
        }
    }

    public LocalDateTime getGeneratedDate(){
        if (this.generatedDate == null){
            this.generatedDate = LocalDateTime.now();
        }
        return this.generatedDate;
    }
}
