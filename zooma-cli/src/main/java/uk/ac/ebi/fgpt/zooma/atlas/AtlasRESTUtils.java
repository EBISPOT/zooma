package uk.ac.ebi.fgpt.zooma.atlas;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of utilities aimed at working with the Atlas Curation REST-API
 *
 * @author Tony Burdett
 * @date 24/11/11
 */
public class AtlasRESTUtils {
    /**
     * Makes an HTTP request to the URL specified by combining the api root with the api path.  The json object that is
     * acquired from this request is returned as a string.  Any communication errors are wrapped in an exception.
     *
     * @param apiRoot the root path of the REST API
     * @param apiPath the specific part of the REST API path to send the request to
     * @return the response content from the server as a string
     * @throws AtlasCommunicationException
     */
    public static String acquireRESTResponse(URL apiRoot, String apiPath) throws AtlasCommunicationException {
        // create new http client
        HttpClient httpClient = new DefaultHttpClient();
        // set proxy, if declared
        if (System.getProperty("http.proxyHost") != null) {
            HttpHost proxy = new HttpHost(
                    System.getProperty("http.proxyHost"),
                    Integer.parseInt(System.getProperty("http.proxyPort")),
                    "http");
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        // create appropriately configured get request
        String address = apiRoot.toString() + apiPath;
        HttpGet get = new HttpGet(address);
        get.addHeader("Accept", "application/json");

        HttpResponse response;
        StringBuilder sb = new StringBuilder();
        try {
            response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                if (response.getStatusLine().getStatusCode() == 200) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(instream));
                        String line;
                        while ((line = in.readLine()) != null) {
                            sb.append(line);
                        }
                        return sb.toString();
                    }
                    finally {
                        instream.close();
                    }
                }
                else {
                    instream.close();
                    throw new AtlasCommunicationException(
                            "Bad response - '" + address + "' returned HTTP " +
                                    response.getStatusLine().getStatusCode());
                }
            }
            else {
                throw new AtlasCommunicationException("No response from '" + address + "'");
            }
        }
        catch (ClientProtocolException e) {
            throw new AtlasCommunicationException("There was a http protocol problem with the Atlas API", e);
        }
        catch (IOException e) {
            throw new AtlasCommunicationException("Failed to read response from the Atlas API", e);
        }
    }

    /**
     * Takes the string 'name' and converts it into a URI-appropriate encoded fragment.  So, spaces are replaced with
     * %20, and other such replacements.
     *
     * @param name the value to encode
     * @return an URL-complient encoded version of the supplied string
     */
    public static String encodeAsURIFragment(String name) {
        return name.replaceAll(" ", "%20").replaceAll("/", "_");
    }

    /**
     * Parses a json object into a simple map, storing key/value pairs.
     *
     * @param jsonString a string encoding a json object
     * @return pairs (key,value) from the parsed query result
     */
    public static Map<String, Object> parseJSON(String jsonString) {
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };

        try {
            return mapper.readValue(jsonString, typeRef);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses a json object into a java object of the supplied type.
     *
     * @param jsonString a string encoding a json object
     * @param type       the class of the object to encode as
     * @param <T>        the type parameter on the class - returned objects will be encoded as this type
     * @return an object representing the deserialized JSON string
     */
    public static <T> T parseJSON(String jsonString, Class<T> type) {
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        try {
            return mapper.readValue(jsonString, type);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
