package uk.ac.pride.ols.web.service.client;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.pride.ols.web.service.config.OLSWsConfigDev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.pride.ols.web.service.model.DataHolder;

import java.util.Iterator;
import java.util.List;
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
    public void testGetTermMetadata() throws Exception {

    }

    @Test
    public void testGetTermXrefs() throws Exception {

    }


    @Test
    public void testGetRootTerms() throws Exception {
        Map<String, String> rootTerms = olsClient.getRootTerms("ms");
        logger.info(rootTerms.toString());
        Assert.assertTrue(rootTerms.containsKey("MS:0000000"));
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
        Map<String, String> children = olsClient.getTermChildren("MS:1001143", "ms", 1);
        logger.info(children.toString());
        Assert.assertTrue(children.containsKey("MS:1001568"));
    }

    @Test
    public void testIsObsolete() throws Exception {

        Boolean obsolete = olsClient.isObsolete("MS:1001057", "ms");
        Assert.assertTrue(obsolete);

    }

    @Test
    public void testGetTermsByAnnotationData() throws Exception {

        List<DataHolder> annotations = olsClient.getTermsByAnnotationData("mod","DiffAvg", 30, 140);

    }

}