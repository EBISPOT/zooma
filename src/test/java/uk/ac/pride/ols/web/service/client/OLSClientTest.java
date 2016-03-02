package uk.ac.pride.ols.web.service.client;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.pride.ols.web.service.config.OLSWsConfigDev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
public class OLSClientTest {

    private static OLSClient olsClient = new OLSClient(new OLSWsConfigDev());
    private static final Logger logger = LoggerFactory.getLogger(OLSClientTest.class);

    @Test
    public void testGetTermById() throws Exception {

        String term = olsClient.getTermByOBOId("MS:1001767", "MS");

        Assert.assertTrue(term.equalsIgnoreCase("nanoACQUITY UPLC System with Technology"));

    }


    @Test
    public void testGetOntologyNames() throws Exception {

        Map<String, String> ontologies = olsClient.getOntologyNames();
        logger.info(ontologies.toString());

        Assert.assertTrue(ontologies.containsKey("ms"));

    }

    @Test
    public void testGetAllTermsFromOntology() throws Exception {

        Map<String, String> terms = olsClient.getAllTermsFromOntology("ms");
        logger.info(terms.toString());

        Assert.assertTrue(terms.containsKey("MS:1001767"));

    }

    @Test
    public void testGetTermByOBOId() throws Exception {

    }

    @Test
    public void testGetTermMetadata() throws Exception {

    }

    @Test
    public void testGetTermXrefs() throws Exception {

    }


    @Test
    public void testGetRootTerms() throws Exception {

    }

    @Test
    public void testGetTermsByName() throws Exception {

        Map<String, String> terms = olsClient.getTermsByName("modification", "ms", false);

        logger.info(terms.toString());

        Assert.assertTrue(terms.containsKey("MS:1001720"));

        terms = olsClient.getTermsByName("modification", "ms", true);

        Iterator iterator = terms.keySet().iterator();

        Assert.assertTrue(((String) iterator.next()).equalsIgnoreCase("MS:1001876"));

    }

    @Test
    public void testGetTermChildren() throws Exception {

    }

    @Test
    public void testIsObsolete() throws Exception {

    }

    @Test
    public void testGetTermsByAnnotationData() throws Exception {

    }

}