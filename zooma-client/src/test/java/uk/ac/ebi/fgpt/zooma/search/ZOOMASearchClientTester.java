package uk.ac.ebi.fgpt.zooma.search;

import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 23/05/14
 */
public class ZOOMASearchClientTester {
    public static void main(String[] args) {
        try {
            ZOOMASearchClient client = new ZOOMASearchClient(new URL("http://wwwdev.ebi.ac.uk/fgpt/zooma"));
            client.annotate(new SimpleTypedProperty("organism part", "head and thorax"));
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
