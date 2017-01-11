package uk.ac.ebi.spot.zooma.service;

import java.io.IOException;
import java.util.List;

/**
 * Created by olgavrou on 19/09/2016.
 */
public interface AnnotationLoadingService<T> {
    void load() throws IOException;
    List<T> returnLoaded();
}
