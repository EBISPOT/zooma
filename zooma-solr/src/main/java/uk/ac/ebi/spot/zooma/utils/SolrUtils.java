package uk.ac.ebi.spot.zooma.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by olgavrou on 30/03/2017.
 */
public class SolrUtils {

    public static <T> boolean listEqualsNoOrder(Collection<T> l1, Collection<T> l2) {
        final Set<T> s1 = new HashSet<>(l1);
        final Set<T> s2 = new HashSet<>(l2);

        return s1.equals(s2);
    }
}
