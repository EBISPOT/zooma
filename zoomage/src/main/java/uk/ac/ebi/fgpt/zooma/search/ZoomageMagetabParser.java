package uk.ac.ebi.fgpt.zooma.search;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mged.magetab.error.ErrorItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.SDRF;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.AssayNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.HybridizationNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SourceNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.arrayexpress2.magetab.renderer.IDFWriter;
import uk.ac.ebi.arrayexpress2.magetab.renderer.SDRFWriter;
import uk.ac.ebi.arrayexpress2.magetab.validator.MAGETABValidator;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.io.*;
import java.util.*;

/**
 * The overall program takes a MAGETAB accession number, parses it using the Limpopo web service and applies
 * automatic curation using the Zooma webservice.
 * Produces two spreadsheets (SDRF and IDF) resulting from the zooma curation process. Currently, IDF is returned
 * only for convenience, no zoomification is applied yet.  //todo:
 * Runs from the command line, and takes several arguments, the only mandatory one is the accession to retrieve from ArrayExpress.
 * The others are in the defaults file and may be overridden as desired.
 *
 * @author Julie McMurry, part of this is derived/adapted from original limpopo demo httpClient by Tony Burdett (12/2011)
 * @date 2013-04-05
 */
public class ZoomageMagetabParser {

    private final String outFileBasePath;
    private Logger log = LoggerFactory.getLogger(getClass());
    private ArrayList<String> comments = new ArrayList<String>();

    private HttpClient httpClient = new DefaultHttpClient();

    private final String limpopoPath;
    private String magetabAccession;
    private final String magetabBasePath;
    private final boolean overwriteValues;
    private final boolean overwriteAnnotations;
    private final boolean stripLegacyAnnotations;


    public ZoomageMagetabParser(String limpopoPath, String magetabBasePath, String outFileBasePath, boolean overwriteValues, boolean overwriteAnnotations, boolean stripLegacyAnnotations) {
        this.limpopoPath = limpopoPath;
        this.magetabBasePath = magetabBasePath;
        this.outFileBasePath = outFileBasePath;
        this.overwriteValues = overwriteValues;
        this.overwriteAnnotations = overwriteAnnotations;
        this.stripLegacyAnnotations = stripLegacyAnnotations;
    }

    protected Logger getLog() {
        return log;
    }

    public boolean runFromFilesystem(String magetabAccession) {

        this.magetabAccession = magetabAccession;
        // pass the magetab accession to the service to fetch json
        File mageTabFile = fetchMAGETABFromFilesystem(magetabAccession);

        try {
            // Parse the file using limpopo
            MAGETABParser parser = new MAGETABParser(new MAGETABValidator());
            // add error item listener that writes to the parser log
            final Set<String> encounteredWarnings = new HashSet<>();

            parser.addErrorItemListener(new ErrorItemListener() {

                public void errorOccurred(ErrorItem item) {
                    getLog().error(item.getErrorCode() + ": " + item.getMesg() + " [line " +
                            item.getLine() + ", column " + item.getCol() + "] (" +
                            item.getComment() + ")");
                    if (item.getErrorCode() != 501) {
                        synchronized (encounteredWarnings) {
                            getLog().error("Warning in file '" + item.getParsedFile() + "'");
                            encounteredWarnings.add(item.getParsedFile());
                        }
                    }
                }
            });

            MAGETABInvestigation investigation = parser.parse(mageTabFile);

            if (!encounteredWarnings.isEmpty()) {
                getLog().warn("\n\n\n============================\n\n\n");
                getLog().warn("Parsing " + investigation.getAccession() + " resulted in " + encounteredWarnings.size() + " warnings - result may not be reliable");
                getLog().warn("\n\n\n============================\n\n\n");
            }

            //zoomify the sdrf
            SDRF newSDRF = zoomifyMAGETAB(investigation.SDRF);
            getLog().debug("\n\n\n============================\n\n\n");
            getLog().info("We parsed magetab and zoomified contents into sdrf representation");

            //write the results to a file

            SDRFWriter sdrfWriter = new SDRFWriter(new FileWriter(outFileBasePath + investigation.getAccession() + ".sdrf.txt"));
            getLog().info("SDRF written to " + outFileBasePath);

            IDFWriter idfWriter = new IDFWriter(new FileWriter(outFileBasePath + investigation.getAccession() + ".idf.txt"));
            getLog().info("IDF written to " + outFileBasePath);

           try {
                // write old IDF
                idfWriter.write(investigation.IDF);
                // but write new SDRF
                // todo: we need to force layout recalculation as we haven't added any new nodes, just changed existing ones
                newSDRF.getLayout().calculateLocations(newSDRF);
                sdrfWriter.write(newSDRF);

                getLog().debug("\n\n\n============================\n\n\n");
                getLog().info("IDF and SDRF files for " + investigation.getAccession() + " written to " + outFileBasePath);
                getLog().debug("\n\n\n============================\n\n\n");
                return true;
            } catch (UnsupportedOperationException e) {
                getLog().error("Zoomifications could not be written to SDRF because of an error: " + e.getMessage());
            }


        } catch (IOException | ParseException e) {
            getLog().error("An error was encountered processing " + magetabAccession + ":" + e.getMessage());
            return false;
        }

        return false;
    }

    /**
     * Creates an SDRF (graph-based) object from the 2D String Arrays, parses the relevant nodes of this object, and
     * delegates zoomification thereof. Returns revised SDRF object.
     * <p/>
     * //     * @param dataAs2DArrays
     *
     * @param sdrf
     * @return SDRF object updated with zooma annotations
     */
//    private SDRF zoomifyMAGETAB(MAGETABSpreadsheet dataAs2DArrays, ZoomaRESTClient zoomaClient) {
    private SDRF zoomifyMAGETAB(SDRF sdrf) {

        // iterate over sourceNodes fetch corresponding zooma annotation, make changes accordingly
        Collection<SourceNode> sourceNodes = sdrf.getNodes(SourceNode.class);

        getLog().info("Processing " + sourceNodes.size() + " Source Nodes ...");


        for (SourceNode sourceNode : sourceNodes) {

            String nodeName = sourceNode.getNodeName();

            getLog().info("Processing source node: " + nodeName);


            for (CharacteristicsAttribute attribute : sourceNode.characteristics) {

                attribute = process(attribute, nodeName);
                getLog().debug("Processed attribute: " + attribute.getAttributeType() + ":" + attribute.getAttributeValue());
            }

        }


        // do the same for sampleNodes
        Collection<SampleNode> sampleNodes = sdrf.getNodes(SampleNode.class);

        getLog().info("Processing " + sampleNodes.size() + " Sample Nodes ...");

        for (SampleNode sampleNode : sampleNodes) {

            String nodeName = sampleNode.getNodeName();

            getLog().info("Processing sample node: " + nodeName);

            for (CharacteristicsAttribute attribute : sampleNode.characteristics) {
                attribute = process(attribute, nodeName);
                getLog().debug("Processed attribute: " + attribute.getAttributeType() + ":" + attribute.getAttributeValue());
            }

        }

        // do the same for hybridizationNodes
        Collection<HybridizationNode> hybridizationNodes = sdrf.getNodes(HybridizationNode.class);

        getLog().info("Processing " + hybridizationNodes.size() + " Hybridization Nodes ...");


        for (HybridizationNode hybridizationNode : hybridizationNodes) {

            String nodeName = hybridizationNode.getNodeName();

            getLog().info("Processing hybridization node: " + nodeName);

            for (FactorValueAttribute attribute : hybridizationNode.factorValues) {

                attribute = process(attribute, nodeName);
                getLog().debug("Processing attribute: " + attribute.getAttributeType() + ":" + attribute.getAttributeValue());

            }

        }

        // do the same for hybridizationNodes
        Collection<AssayNode> assayNodes = sdrf.getNodes(AssayNode.class);

        getLog().info("Processing " + assayNodes.size() + " Assay Nodes ...");

        for (AssayNode assayNode : assayNodes) {
            String nodeName = assayNode.getNodeName();

            getLog().info("Processing assay node: " + nodeName);

            for (FactorValueAttribute attribute : assayNode.factorValues) {

                process(attribute, nodeName);
            }
        }

        return sdrf;

    }

    private CharacteristicsAttribute process(CharacteristicsAttribute attribute, String bioentity) {

        // First create the baseline transitional attribute before stripping legacy annotations.
        TransitionalAttribute baselineTransAttribute = new TransitionalAttribute(magetabAccession, bioentity, attribute);

        // then strip legacy annotations if indicated
        if (stripLegacyAnnotations) {
            attribute.termSourceREF = null;
            attribute.termAccessionNumber = null;
        }

        // First check exclusions based on the unmodified attribute
        if (ZoomageUtils.excludeAttribute(baselineTransAttribute)) {
            // if found in exclusions cache, item should be excluded,
            //then return the attribute without making any other changes
            return attribute;
        }

        TransitionalAttribute zoomifiedTransAttribute = ZoomageUtils.getZoomaResults(baselineTransAttribute);
        attribute = zoomify(zoomifiedTransAttribute, attribute);

        // return the attribute whether or not it has been modified
        return attribute;

    }

    private FactorValueAttribute process(FactorValueAttribute attribute, String nodeName) {

        // First create the baseline transitional attribute before stripping legacy annotations.
        TransitionalAttribute baselineTransAttribute = new TransitionalAttribute(magetabAccession, nodeName, attribute);

        // then strip legacy annotations if indicated
        if (stripLegacyAnnotations) {
            attribute.termSourceREF = null;
            attribute.termAccessionNumber = null;
        }

        // First check exclusions based on the unmodified attribute
        if (ZoomageUtils.excludeAttribute(baselineTransAttribute)) {
            // if found in exclusions cache, item should be excluded,
            //then return the attribute without making any other changes
            return attribute;
        }

        TransitionalAttribute zoomifiedTransAttribute = ZoomageUtils.getZoomaResults(baselineTransAttribute);
        attribute = zoomify(zoomifiedTransAttribute, attribute);

        // return the attribute whether or not it has been modified
        return attribute;
    }

    private CharacteristicsAttribute zoomify(TransitionalAttribute zoomifiedAttribute, CharacteristicsAttribute attribute) {
        if (zoomifiedAttribute == null) {
            getLog().warn("Zoomified Attribute is null for " + attribute.getAttributeType() + attribute.getAttributeValue());
            return null;
        }
        AnnotationSummary annotationSummary = zoomifiedAttribute.getAnnotationSummary();
        if (annotationSummary == null) return attribute;

        // if we should overwrite the term source preliminaryStringValue, do so
        if (overwriteValues) {
            getLog().warn("Overwriting " + attribute.getAttributeValue() + " with " + zoomifiedAttribute.getZoomifiedTermValue());
            attribute.setAttributeValue(zoomifiedAttribute.getZoomifiedTermValue());
        }

        // if we should overwrite annotations, or if annotations are missing, apply zoomified annotations
        if (overwriteAnnotations || attribute.termSourceREF == null || attribute.termSourceREF.equals("")) {

            // todo: check... IF the zoomified annotation is null, then this update effectively strips existing annotations
            attribute.termSourceREF = zoomifiedAttribute.getZoomifiedTermSourceREF();
            attribute.termAccessionNumber = zoomifiedAttribute.getZoomifiedOntAccession();
        }

        return attribute;

    }


    private FactorValueAttribute zoomify(TransitionalAttribute zoomifiedAttribute, FactorValueAttribute attribute) {

        AnnotationSummary annotationSummary = zoomifiedAttribute.getAnnotationSummary();

        if (annotationSummary == null) return attribute;

        // if we should overwrite the term source preliminaryStringValue, do so
        if (overwriteValues) {
            getLog().warn("Overwriting " + attribute.getAttributeValue() + " with " + zoomifiedAttribute.getZoomifiedTermValue());
            attribute.setAttributeValue(zoomifiedAttribute.getZoomifiedTermValue());
        }

        // if we should overwrite annotations, or if annotations are missing, apply zoomified annotations
        if (overwriteAnnotations || attribute.termSourceREF == null || attribute.termSourceREF.equals("")) {

            // todo: check... IF the zoomified annotation is null, then this update effectively strips existing annotations
            attribute.termSourceREF = zoomifiedAttribute.getZoomifiedTermSourceREF();
            attribute.termAccessionNumber = zoomifiedAttribute.getZoomifiedOntAccession();
        }

        return attribute;
    }

    private void appendComment(String varName, String oldString, String newString) {

//        if (buildComments) {
        // if there's no new string, just return
        if (newString == null || newString.equals("")) return;

        // else, initialize comment
        String comment = "";

        // if there's no original annotation, phrase the comment accordingly
        if (oldString == null || oldString.equals("")) comment = (varName + " set to " + newString + ".");

        getLog().debug(comment);

        // finally, append the comment
        comments.add(comment);
//        } else getLog().error("The 'appendComment method' was probably invoked in error.");
    }

    public File fetchMAGETABFromFilesystem(String accession) {
        String pipeline = accession.split("-")[1];
        return new File(magetabBasePath + File.separator + pipeline + File.separator + accession + File.separator + accession +
                ".idf.txt");
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
        String postURL = limpopoPath + "/api/magetab";
        HttpPost post = new HttpPost(postURL);

        return executePost(post, idf, sdrf);
    }

    /**
     * Takes a 2D string array (idf or sdrf) and coverts it to json
     *
     * @param idfOrSdrf
     * @return json as string
     */
    public String convert2DStringArrayToJSON(String[][] idfOrSdrf) {
        try {
            JsonFactory jsonFactory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(jsonFactory);

            StringWriter out = new StringWriter();
            JsonGenerator jg = jsonFactory.createJsonGenerator(out);

            mapper.writeValue(jg, idfOrSdrf);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Takes a 2D string array (idf or sdrf) and coverts it to input stream
     *
     * @param idfOrSdrf
     * @return input stream
     */
    public InputStream convert2DStringArrayToStream(String[][] idfOrSdrf) {
        StringBuilder sb = new StringBuilder();
        for (String[] row : idfOrSdrf) {
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

    /**
     * Takes an HttpGet and returns the corresponding optMessage body as a string
     *
     * @param get
     * @return optMessage body as a string
     */
    public String executeGet(HttpGet get) {
        try {

            HttpResponse response = httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            // To get the respose body as a String though by not using a responseHandler I get it first as InputStream:
            InputStream inputStream = response.getEntity().getContent();
            String responseString = IOUtils.toString(inputStream, "UTF-8");

            // Execute the method.
            if (statusCode != HttpStatus.SC_OK) {
                getLog().error("Method failed: " + response.getStatusLine());
            } else {
                getLog().debug("Sent GET request to " + get.getURI() + ", response code " + statusCode);
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

    /**
     * @param post
     * @param idf
     * @param sdrf
     * @return magetab as json
     */
    public String executePost(HttpPost post, String[][] idf, String[][] sdrf) {

        // create json objects from the IDF and SDRF parts of the spreadsheet
        String idfJson = "\"idf\":   " + convert2DStringArrayToJSON(idf);
        String sdrfJson = "\"sdrf\":   " + convert2DStringArrayToJSON(sdrf);

        getLog().debug("execute post");
        getLog().debug(idfJson);
        System.out.println("\n\n-------------------------------------\n\n");
        getLog().debug(sdrfJson);
        System.out.println("\n\n-------------------------------------\n\n");


        post.setHeader("Content-Type", "application/json");

        post.getParams().setParameter("idf", idfJson);
//        post.getParams().setParameter("sdrf", sdrfJson);

        try {

            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            InputStream inputStream = response.getEntity().getContent();
            String responseString = IOUtils.toString(inputStream, "UTF-8");

            // Execute the method.
            if (statusCode != HttpStatus.SC_OK) {
                getLog().error("Method failed: " + response.getStatusLine());
            } else {
                getLog().debug("Sent post request to " + post.getURI() + ", response code " + statusCode);
            }

            // Read the response body.
            getLog().debug("JSON Response String \n" + responseString);
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
//                     getLog().info("This request generated a valid response with application/json response type");
                    return true;
                }
            }
        }
        throw new RuntimeException("Unexpected response type: should be application/json");
    }


    /**
     * Internal class to store the data in two 2D arrays corresponding to the idf and sdrf objects
     */
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


    public ArrayList<String> getComments() {
        return comments;
    }


}