package uk.ac.ebi.fgpt.zooma.util;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 18/02/13
 */
public class TestCollectionUtils {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Object item1;
    private Object item2;

    private Object equalItem1;
    private Object equalItem2;

    private Object unequalItem1;
    private Object unequalItem2;

    @BeforeEach
    public void setup() {
        logger.trace("Initializing item1");
        item1 = new Object() {
            @Override public int hashCode() {
                return 1;
            }

            @Override public boolean equals(Object obj) {
                return obj == item1 || obj == equalItem1;
            }
        };

        item2 = new Object() {
            @Override public int hashCode() {
                return 2;
            }

            @Override public boolean equals(Object obj) {
                return obj == item2 || obj == equalItem2;
            }
        };

        equalItem1 = new Object() {
            @Override public int hashCode() {
                return 1;
            }

            @Override public boolean equals(Object obj) {
                return obj == item1 || obj == equalItem1;
            }
        };

        equalItem2 = new Object() {
            @Override public int hashCode() {
                return 2;
            }

            @Override public boolean equals(Object obj) {
                return obj == item2 || obj == equalItem2;
            }
        };

        unequalItem1 = new Object();
        unequalItem2 = new Object();
    }

    @AfterEach
    public void teardown() {
        item1 = null;
        item2 = null;
        equalItem1 = null;
        equalItem2 = null;
        unequalItem1 = null;
        unequalItem2 = null;
    }

    @Test
    public void testSame() {
        Collection coll = Arrays.asList(item1, item2);
        assertTrue(CollectionUtils.compareCollectionContents(coll, coll), "Comparing identical collections should be return true");
    }

    @Test
    public void testAllEqual() {
        Collection coll1 = Arrays.asList(item1, item2);
        Collection coll2 = Arrays.asList(equalItem1, equalItem2);
        assertTrue(CollectionUtils.compareCollectionContents(coll1, coll2), "Comparing equal collections should be return true");

        Collection coll3 = Arrays.asList(item1, item2);
        Collection coll4 = Arrays.asList(equalItem2, equalItem1);
        assertTrue(CollectionUtils.compareCollectionContents(coll3, coll4), "Comparing equal but differently ordered collections should be return true");

        List<Object> coll5 = new ArrayList<>();
        coll5.add(item1);
        coll5.add(item2);
        Set<Object> coll6 = new HashSet<>();
        coll6.add(equalItem1);
        coll6.add(equalItem2);
        assertTrue(CollectionUtils.compareCollectionContents(coll5, coll6), "Comparing differently typed collections with same elements should be return true");
    }

    @Test
    public void testNotAllEqual() {
        Collection coll1 = Arrays.asList(item1, item2);
        Collection coll2 = Arrays.asList(equalItem1, unequalItem2);
        assertFalse(CollectionUtils.compareCollectionContents(coll1, coll2), "Comparing unequal collections should be return false");
    }

    @Test
    public void testAllDifferent() {
        Collection coll1 = Arrays.asList(item1, item2);
        Collection coll2 = Arrays.asList(unequalItem1, unequalItem2);
        assertFalse(CollectionUtils.compareCollectionContents(coll1, coll2), "Comparing unequal collections should be return false");
    }

    @Test
    public void testEmpty() {
        Collection coll1 = Collections.emptyList();
        Collection coll2 = Collections.emptyList();
        assertTrue(CollectionUtils.compareCollectionContents(coll1, coll2), "Empty collections should be equal");
    }
}
