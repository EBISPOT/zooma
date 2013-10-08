package uk.ac.ebi.fgpt.zooma.util;

import java.util.Collection;

/**
 * Some simple utilities for working with collections
 *
 * @author Tony Burdett
 * @date 28/05/12
 */
public class CollectionUtils {
    public static <C extends Collection> boolean compareCollectionContents(C c1, C c2) {
        if (c1.size() != c2.size()) {
            return false;
        }
        else {
            boolean allMatched = true;
            // make sure c2 contains all elements in c1
            for (Object o : c1) {
                if (!c2.contains(o)) {
                    allMatched = false;
                    break;
                }
            }

            // and make sure c1 contains all elements in c2
            for (Object o : c2) {
                if (!c1.contains(o)) {
                    allMatched = false;
                    break;
                }
            }

            return allMatched;
        }
    }
}
