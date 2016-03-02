package uk.ac.pride.ols.web.service.client;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.pride.ols.web.service.config.OLSWsConfigDev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}