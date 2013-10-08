package uk.ac.ebi.fgpt.zooma.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 18/02/13
 */
public class TestCollectionUtils {
    private Object item1;
    private Object item2;

    private Object equalItem1;
    private Object equalItem2;

    private Object unequalItem1;
    private Object unequalItem2;

    @Before
    public void setup() {
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

    @After
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
        assertTrue("Comparing identical collections should be return true",
                   CollectionUtils.compareCollectionContents(coll, coll));
    }

    @Test
    public void testAllEqual() {
        Collection coll1 = Arrays.asList(item1, item2);
        Collection coll2 = Arrays.asList(equalItem1, equalItem2);
        assertTrue("Comparing equal collections should be return true",
                   CollectionUtils.compareCollectionContents(coll1, coll2));

        Collection coll3 = Arrays.asList(item1, item2);
        Collection coll4 = Arrays.asList(equalItem1, equalItem2);
        assertTrue("Comparing equal but differently ordered collections should be return true",
                   CollectionUtils.compareCollectionContents(coll3, coll4));

        List<Object> coll5 = new ArrayList<>();
        coll5.add(item1);
        coll5.add(item2);
        Set<Object> coll6 = new HashSet<>();
        coll6.add(equalItem1);
        coll6.add(equalItem2);
        assertTrue("Comparing differently typed collections with same elements should be return true",
                   CollectionUtils.compareCollectionContents(coll5, coll6));
    }

    @Test
    public void testNotAllEqual() {
        Collection coll1 = Arrays.asList(item1, item2);
        Collection coll2 = Arrays.asList(equalItem1, unequalItem2);
        assertFalse("Comparing unequal collections should be return false",
                    CollectionUtils.compareCollectionContents(coll1, coll2));
    }

    @Test
    public void testAllDifferent() {
        Collection coll1 = Arrays.asList(item1, item2);
        Collection coll2 = Arrays.asList(unequalItem1, unequalItem2);
        assertFalse("Comparing unequal collections should be return false",
                    CollectionUtils.compareCollectionContents(coll1, coll2));
    }

    @Test
    public void testEmpty() {
        Collection coll1 = Collections.emptyList();
        Collection coll2 = Collections.emptyList();
        assertTrue("Empty collections should be equal", CollectionUtils.compareCollectionContents(coll1, coll2));
    }
}
