package uk.ac.ebi.fgpt.zooma.search;

//import org.apache.commons.httpclient.*;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.params.HttpMethodParams;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.SDRF;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.HybridizationNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SourceNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.magetab.parser.SDRFParser;
import uk.ac.ebi.arrayexpress2.magetab.renderer.SDRFWriter;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The overall program takes a MAGETAB accession number, parses it using the Limpopo web service and applies
 * automatic curation using the Zooma webservice.
 * Produces two spreadsheets (SDRF and IDF) resulting from the zooma curation process. Currently, IDF is returned
 * only for convenience, no zoomification is applied yet.  //todo:
 * Runs from the command line, and takes three arguments - the accession to retrieve from ArrayExpress,
 * the threshold for the minimum string length to initiate a zooma query, and the zooma cutoff percentage (eg .8
 * discards the bottom 80% of matches and retains just the top 20%. //todo: tony to confirm
 *
 * @author Julie McMurry, part of this is derived/adapted from original limpopo demo client by Tony Burdett (12/2011)
 * @date 2013-04-05
 */
public class ZooMAGE {

    private HttpClient client = new DefaultHttpClient();
    //todo: update handling to new version of http client
    private static HashMap<String, AnnotationSummary> resultsCache = new HashMap<String, AnnotationSummary>();

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    /**
     * Get input from user via commandline
     *
     * @param args [0] MAGETAB accessino number,
     *             [1] minimum length of a string to query zooma for a match
     *             [2] Zooma cutoff percentage
     */
    public static void main(String[] args) {
        try {
            ZooMAGE zooMAGE = new ZooMAGE();

            String MAGETABaccession = args[0];
            int minInputLength = Integer.parseInt(args[1]);
            float zoomaCutoffPercentage = Float.parseFloat(args[2]);

            ZoomaRESTClient zoomaClient = new ZoomaRESTClient(minInputLength, zoomaCutoffPercentage);

            zooMAGE.run(MAGETABaccession, zoomaClient);

        } catch (Exception e) {
            //todo: input help
            System.err.print("Application failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Execute the program from the parameters collected in the main method
     *
     * @param MAGETABaccession
     * @param zoomaClient
     */
    public void run(String MAGETABaccession, ZoomaRESTClient zoomaClient) {

        // pass accession to the service to fetch json
        String magetabAsJson = fetchMAGETAB(MAGETABaccession);
        getLog().info("We sent a GET request to the server for " + MAGETABaccession + " and got the following response:");
        getLog().info(magetabAsJson);
        getLog().info("\n\n\n============================\n\n\n");

        // Parse the json into a simple magetab representation (two 2D String arrays)
        MAGETABSpreadsheet dataAs2DArrays = JSONtoMAGETAB(magetabAsJson);
        getLog().info("\n\n\n============================\n\n\n");
        getLog().info("We parsed json into magetab representation");

        try {
            SDRF sdrf = zoomifyMAGETAB(dataAs2DArrays, zoomaClient);
            getLog().info("\n\n\n============================\n\n\n");
            getLog().info("We parsed magetab and zoomified contents into sdrf representation");
            Writer outFileWriter = new FileWriter(MAGETABaccession + ".txt");
            SDRFWriter sdrfWriter = new SDRFWriter(outFileWriter);
            sdrfWriter.write(sdrf);

            getLog().info("\n\n\n============================\n\n\n");
            getLog().info("We wrote sdrf to file.");

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        log.info("\n\n\n============================\n\n\n");

        // now, post these 2D string arrays to the server and collect any error items
        String postJson = sendMAGETAB(dataAs2DArrays.getIdf(), dataAs2DArrays.getSdrf());
        log.info("We POSTed IDF and SDRF json objects to the server, and got the following response:");
        log.info(postJson);
        log.info("\n\n\n============================\n\n\n");

        // now, extract error items from the resulting JSON   //todo: this throws an error
//        List<Map<String, Object>> errors = parseErrorItems(postJson);
//        log.info("Our POST operation has returned " + errors.size() + " error items, these follow:");
//        for (Map<String, Object> error : errors) {
//            log.error("\t" +
//                    error.get("code") + ":\t" +
//                    error.get("comment") + "\t[line " +
//                    error.get("line") + ", column" +
//                    error.get("column") + "]");
//        }
    }

    /**
     * Creates an SDRF (graph-based) object from the 2D String Arrays, parses the relevant nodes of this object, and
     * delegates zoomification thereof. Returns revised SDRF object.
     *
     * @param dataAs2DArrays
     * @param zoomaClient
     * @return SDRF object updated with zooma annotations
     */
    private SDRF zoomifyMAGETAB(MAGETABSpreadsheet dataAs2DArrays, ZoomaRESTClient zoomaClient) {

        InputStream in = convert2DStringArrayToStream(dataAs2DArrays.getSdrf());

        try {
            SDRF sdrf = new SDRFParser().parse(in);

            // iterate over sourceNodes fetch corresponding zooma annotation, make changes accordingly
            Collection<SourceNode> sourceNodes = sdrf.getNodes(SourceNode.class);
            for (SourceNode sourceNode : sourceNodes) {
                System.out.println("\n--------------------------------------------------------------");
                getLog().info("SourceNode: " + sourceNode.getNodeName());
                for (CharacteristicsAttribute attribute : sourceNode.characteristics) {

                    TransitionalAttribute transitionalAttribute = zoomifyAttribute(attribute, zoomaClient);

                    attribute.setAttributeValue(transitionalAttribute.getValue());
                    attribute.termSourceREF = transitionalAttribute.getTermSourceREF();
                    attribute.termAccessionNumber = transitionalAttribute.getTermAccessionNumber();

                    sourceNode.comments.put("Zoomifications", transitionalAttribute.getComments());
                }
            }

            // do the same for sampleNodes
            Collection<SampleNode> sampleNodes = sdrf.getNodes(SampleNode.class);
            for (SampleNode sampleNode : sampleNodes) {
                getLog().debug(sampleNode.getNodeName());
                for (CharacteristicsAttribute attribute : sampleNode.characteristics) {

                    TransitionalAttribute transitionalAttribute = zoomifyAttribute(attribute, zoomaClient);

                    attribute.setAttributeValue(transitionalAttribute.getValue());
                    attribute.termSourceREF = transitionalAttribute.getTermSourceREF();
                    attribute.termAccessionNumber = transitionalAttribute.getTermAccessionNumber();

                    sampleNode.comments.put("Zoomifications", transitionalAttribute.getComments());
                }
            }

            // do the same for hybridizationNodes
            Collection<HybridizationNode> hybridizationNodes = sdrf.getNodes(HybridizationNode.class);
            for (HybridizationNode hybridizationNode : hybridizationNodes) {
                getLog().debug(hybridizationNode.getNodeName());
                for (FactorValueAttribute attribute : hybridizationNode.factorValues) {

                    TransitionalAttribute transitionalAttribute = zoomifyAttribute(attribute, zoomaClient);

                    attribute.setAttributeValue(transitionalAttribute.getValue());
                    attribute.termSourceREF = transitionalAttribute.getTermSourceREF();
                    attribute.termAccessionNumber = transitionalAttribute.getTermAccessionNumber();

                    hybridizationNode.comments.put("Zoomifications", transitionalAttribute.getComments());
                }
            }

            return sdrf;

        } catch (ParseException e) {
            e.printStackTrace();  //todo
        }

        return null;
    }

    private TransitionalAttribute zoomifyAttribute(FactorValueAttribute attribute, ZoomaRESTClient zoomaClient) {

        return zoomaClient.zoomify(new TransitionalAttribute(attribute));
    }

    private TransitionalAttribute zoomifyAttribute(CharacteristicsAttribute attribute, ZoomaRESTClient zoomaClient) {
        return zoomaClient.zoomify(new TransitionalAttribute(attribute));
    }

    /**
     * Sends request to Limpopo retrieves json for 'accession'
     *
     * @param accession MAGETAB accession
     * @return String in json format
     */
    public String fetchMAGETAB(String accession) {
        // send a request to limpopo
        String getURL = "http://wwwdev.ebi.ac.uk/fgpt/limpopo/api/accessions/" + accession;
        HttpGet httpget = new HttpGet(getURL);
        String responseBody = "";

        DefaultHttpRequestRetryHandler defaultHttpRequestRetryHandler = new DefaultHttpRequestRetryHandler(3, false);

        BasicHttpParams params = new BasicHttpParams();

        params.setParameter("format", "json");
        try {
            httpget.setParams(params);
            HttpResponse response = client.execute(httpget);
            responseBody = EntityUtils.toString(response.getEntity());
            getLog().info("runURL", "response " + responseBody); //prints the complete HTML code of the web-page
        } catch (Exception e) {
            e.printStackTrace();
        }
//        get.setHttpRequestRetryHandler(retryHandler);
//        get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//                defaultHttpRequestRetryHandler);

        // execute the method method to retrieve json for 'accession'
        return executeGet(httpget);
    }

    /**
     * Creates post method for sending json back to Limpopo
     *
     * @param idf  2d array representation
     * @param sdrf 2d array representation
     * @return
     */
    public String sendMAGETAB(String[][] idf, String[][] sdrf) {
        // create post method for sending json back
        String postURL = "http://wwwdev.ebi.ac.uk/fgpt/limpopo/api/magetab";
        HttpPost post = new HttpPost(postURL);

        return executePost(post, idf, sdrf);
    }

    public String convert2DStringArrayToJSON(String[][] in) {
        try {
            JsonFactory jsonFactory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(jsonFactory);

            StringWriter out = new StringWriter();
            JsonGenerator jg = jsonFactory.createJsonGenerator(out);

            mapper.writeValue(jg, in);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream convert2DStringArrayToStream(String[][] values) {
        StringBuilder sb = new StringBuilder();
        for (String[] row : values) {
            for (String col : row) {
                sb.append(col).append("\t");
            }
            sb.append("\n");
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    /**
     * Converts json object to MAGETABSpreadsheet representation (object containing two 2D string arrays,
     * one for IDF and one for SDRF).
     *
     * @param json
     * @return MAGETAB spreadsheet.
     */
    public MAGETABSpreadsheet JSONtoMAGETAB(String json) {
        try {
            // parse json
            JsonFactory jsonFactory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(jsonFactory);
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };

            HashMap<String, Object> requestResults = new HashMap<String, Object>();
            requestResults = mapper.readValue(json, typeRef);

            List<List<String>> idfObject = (List<List<String>>) requestResults.get("idf");
            List<List<String>> sdrfObject = (List<List<String>>) requestResults.get("sdrf");

            String[][] idf = new String[idfObject.size()][];
            int i = 0;
            for (List<String> row : idfObject) {
                String[] rowArr = new String[row.size()];
                idf[i] = row.toArray(rowArr);
                i++;
            }

            String[][] sdrf = new String[sdrfObject.size()][];
            int j = 0;
            for (List<String> row : sdrfObject) {
                String[] rowArr = new String[row.size()];
                sdrf[j] = row.toArray(rowArr);
                j++;
            }

            return new MAGETABSpreadsheet(idf, sdrf);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> parseErrorItems(String json) {
        try {
            // parse json
            JsonFactory jsonFactory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(jsonFactory);
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };

            HashMap<String, Object> requestResults = new HashMap<String, Object>();
            requestResults = mapper.readValue(json, typeRef);

            return (List<Map<String, Object>>) requestResults.get("errors");
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String executeGet(HttpGet get) {
        try {

            HttpResponse response = client.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            // To get the respose body as a String though by not using a responseHandler I get it first as InputStream:
            InputStream inputStream = response.getEntity().getContent();
            String responseString = IOUtils.toString(inputStream, "UTF-8");

            // Execute the method.
            if (statusCode != HttpStatus.SC_OK) {
                log.error("Method failed: " + response.getStatusLine());
            } else {
                log.info("Sent GET request to " + get.getURI() + ", response code " + statusCode);
            }

            // Read the response body.
            return responseString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Release the connection.
            get.releaseConnection();
        }
    }

    public String executePost(HttpPost post, String[][] idf, String[][] sdrf) {
        // create json objects from the IDF and SDRF parts of the spreadsheet
        String idfJson = "\"idf\":   "+ convert2DStringArrayToJSON(idf);
        String sdrfJson = "\"sdrf\":   "+convert2DStringArrayToJSON(sdrf);

        log.info("execute post");
        log.info(idfJson);
        System.out.println("\n\n-------------------------------------\n\n");
        log.info(sdrfJson);

        post.setHeader("Content-Type", "application/json");

        post.getParams().setParameter("idf", idfJson);
        post.getParams().setParameter("sdrf", sdrfJson);

        try {

            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            InputStream inputStream = response.getEntity().getContent();
            String responseString = IOUtils.toString(inputStream, "UTF-8");

            // Execute the method.
            if (statusCode != HttpStatus.SC_OK) {
                log.error("Method failed: " + response.getStatusLine());
            } else {
                log.info("Sent post request to " + post.getURI() + ", response code " + statusCode);
            }

            // Read the response body.
            getLog().info("JSON Response String \n" + responseString);
            return responseString;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            // Release the connection.
            post.releaseConnection();
        }
    }

    public boolean hasJsonHeader(HttpResponse response) {
        Header[] headers = response.getAllHeaders();
        for (Header header : headers) {
            if (header.getName().contains("Content-Type")) {
                // check we have a json response
                if (header.getValue().contains("application/json")) {
                    // all ok, method response body and convert to json
                    log.info("This request generated a valid response with application/json response type");
                    return true;
                }
            }
        }
        throw new RuntimeException("Unexpected response type: should be application/json");
    }

    public class MAGETABSpreadsheet {
        private String[][] idf;
        private String[][] sdrf;

        public MAGETABSpreadsheet(String[][] idf, String[][] sdrf) {
            this.idf = idf;
            this.sdrf = sdrf;
        }

        public String[][] getIdf() {
            return idf;
        }

        public String[][] getSdrf() {
            return sdrf;
        }
    }


}