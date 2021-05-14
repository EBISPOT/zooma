package uk.ac.ebi.fgpt.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Some simple utilities for working with collections
 *
 * @author Tony Burdett
 * @date 28/05/12
 */
public class CollectionUtils {
    private static Logger logger = LoggerFactory.getLogger(CollectionUtils.class);

    public static <C extends Collection> boolean compareCollectionContents(C c1, C c2) {
        if (c1.size() != c2.size()) {
            logger.trace("Size of c1=" + c1.size() + " does not match size of c2=" + c2.size());
            return false;
        }
        else {
            boolean allMatched = true;
            // make sure c2 contains all elements in c1
            for (Object o : c1) {
                if (!c2.contains(o)) {
                    logger.trace(c2 + " does not contain o=" + o);
                    allMatched = false;
                    break;
                }
            }

            // and make sure c1 contains all elements in c2
            for (Object o : c2) {
                if (!c1.contains(o)) {
                    logger.trace(c1 + " does not contain o=" + o);
                    allMatched = false;
                    break;
                }
            }

            return allMatched;
        }
    }
}
