package uk.ac.ebi.spot.datasource;

import java.io.IOException;
import java.util.List;

/**
 * Created by olgavrou on 19/09/2016.
 */
public interface LoaderDAO<T> {
    void load() throws IOException;
    List<T> returnLoaded();
}
