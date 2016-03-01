package uk.ac.pride.ols.web.service.client;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.pride.ols.web.service.config.OLSWsConfigDev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
public class OLSClientTest {

    private static OLSClient olsClient = new OLSClient(new OLSWsConfigDev());
    private static final Logger logger = LoggerFactory.getLogger(OLSClientTest.class);

    @Test
    public void testGetTermById() throws Exception {

        String term = olsClient.getTermById("MS:1001767", "MS");

        Assert.assertTrue(term.equalsIgnoreCase("nanoACQUITY UPLC System with Technology"));

    }


}