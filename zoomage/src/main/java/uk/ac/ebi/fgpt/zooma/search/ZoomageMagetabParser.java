package uk.ac.ebi.fgpt.zooma.search;

//import org.apache.commons.httpclient.*;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.params.HttpMethodParams;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
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
import org.mged.magetab.error.ErrorItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.SDRF;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.HybridizationNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SourceNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.arrayexpress2.magetab.parser.SDRFParser;
import uk.ac.ebi.arrayexpress2.magetab.renderer.IDFWriter;
import uk.ac.ebi.arrayexpress2.magetab.renderer.SDRFWriter;
import uk.ac.ebi.arrayexpress2.magetab.validator.MAGETABValidator;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
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

    private Logger log = LoggerFactory.getLogger(getClass());
    private ArrayList<String> comments = new ArrayList<String>();

    private final HttpClient httpClient = new DefaultHttpClient();
    private final ZOOMASearchClient zoomaClient;

    private HashMap<String, AnnotationSummary> cacheOfZoomaResultsApplied;
    private HashSet<String> cacheOfLegacyAnnotations;
    private HashSet<String> cacheOfItemsWithNoResults;
    private HashMap<String, Integer> cacheOfItemsRequiringCuration;
    private HashMap<String, boolean[]> cacheOfExclusionsApplied;

    private HashSet<String> exclusionProfiles;

    private final String zoomaPath;
    private final String limpopoPath;
    private String magetabAccession;
    private final String magetabBasePath;
    private final float cutoffScore;
    private final float cutoffPercentage;
    private final int minStringLength;  // todo: move this to the zooma search rest httpClient
    private final boolean olsShortIds;
    private final boolean overwriteValues;
    private final boolean addCommentsToSDRF;
    private final boolean overwriteAnnotations;
    private final String logFileDelimiter;
    private final String compoundAnnotationDelimiter;


    public ZoomageMagetabParser(String zoomaPath, String limpopoPath, String magetabBasePath, int minStringLength, Float cutoffPercentage, Float cutoffScore, boolean olsShortIds, String compoundAnnotationDelimiter, String logFileDelimiter, boolean overwriteValues, boolean overwriteAnnotations, boolean addCommentsToSDRF) {
        this.zoomaPath = zoomaPath;
        this.limpopoPath = limpopoPath;
        this.magetabBasePath = magetabBasePath;
        this.minStringLength = minStringLength;
        this.cutoffPercentage = cutoffPercentage;
        this.cutoffScore = cutoffScore;
        this.olsShortIds = olsShortIds;
        this.overwriteValues = overwriteValues;
        this.overwriteAnnotations = overwriteAnnotations;
        this.addCommentsToSDRF = addCommentsToSDRF;
        this.logFileDelimiter = logFileDelimiter;
        this.compoundAnnotationDelimiter = compoundAnnotationDelimiter;

        try {
            this.zoomaClient = new ZOOMASearchClient(URI.create(zoomaPath).toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create ZOOMASearchCLient", e);
        }
    }

    protected Logger getLog() {
        return log;
    }


    /**
     * Execute the program from the parameters collected in the main method
     *
     * @deprecated
     */
    @Deprecated
    public void runFromWebservice(String magetabAccession) {

        initiatingCaches();

        this.magetabAccession = magetabAccession;

        // pass the magetab accession to the service to fetch json
        String magetabAsJson = fetchMAGETABFromWebservice(magetabAccession);
        getLog().debug("We sent a GET request to the server for " + magetabAccession + " and got the following response:");
        getLog().debug(magetabAsJson);
        getLog().debug("\n\n\n============================\n\n\n");

        // Parse the json into a simple magetab representation (two 2D String arrays)
        MAGETABSpreadsheet dataAs2DArrays = JSONtoMAGETAB(magetabAsJson);
        getLog().debug("\n\n\n============================\n\n\n");
        getLog().debug("We parsed json into magetab representation");

        try {
            // read string array into SDRF
            InputStream in = convert2DStringArrayToStream(dataAs2DArrays.getSdrf());
            SDRF sdrf = new SDRFParser().parse(in);

            //zoomify the sdrf
            SDRF newSDRF = zoomifyMAGETAB(sdrf);  //todo
            getLog().debug("\n\n\n============================\n\n\n");
            getLog().debug("We parsed magetab and zoomified contents into sdrf representation");

            //write the results to a file
            File outfile = new File(magetabAccession + ".sdrf.txt");
            Writer outFileWriter = new FileWriter(outfile);
            SDRFWriter sdrfWriter = new SDRFWriter(outFileWriter);
            sdrfWriter.write(newSDRF);

            getLog().info("\n\n\n============================\n\n\n");
            getLog().info("We wrote sdrf to " + outfile.getAbsolutePath());

            //todo: IDF too

        } catch (IOException | ParseException e) {
            e.printStackTrace();  //todo
        }


        getLog().info("\n\n\n============================\n\n\n");

        // now, post these 2D string arrays to the server and collect any error items
        String postJson = sendMAGETAB(dataAs2DArrays.getIdf(), dataAs2DArrays.getSdrf());
        getLog().debug("We POSTed IDF and SDRF json objects to the server, and got the following response:");
        getLog().debug(postJson);
        getLog().debug("\n\n\n============================\n\n\n");


        // now, extract error items from the resulting JSON   //todo: this throws an error
        List<Map<String, Object>> errors = parseErrorItems(postJson);
        log.debug("Our POST operation has returned " + errors.size() + " error items, these follow:");
        for (Map<String, Object> error : errors) {
            log.error("\t" +
                    error.get("code") + ":\t" +
                    error.get("comment") + "\t[line " +
                    error.get("line") + ", column" +
                    error.get("column") + "]");
        }
    }

    public boolean runFromFilesystem(String magetabAccession) {

        initiatingCaches();

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
                    log.error(item.getErrorCode() + ": " + item.getMesg() + " [line " +
                            item.getLine() + ", column " + item.getCol() + "] (" +
                            item.getComment() + ")");
                    if (item.getErrorCode() != 501) {
                        synchronized (encounteredWarnings) {
                            log.debug("Error in file '" + item.getParsedFile() + "'");
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
            getLog().debug("We parsed magetab and zoomified contents into sdrf representation");

            //write the results to a file
            IDFWriter idfWriter = new IDFWriter(new FileWriter(investigation.getAccession() + ".idf.txt"));
            SDRFWriter sdrfWriter = new SDRFWriter(new FileWriter(investigation.getAccession() + ".sdrf.txt"));

            // write old IDF
            idfWriter.write(investigation.IDF);
            // but write new SDRF
            // todo: we need to force layout recalculation as we haven't added any new nodes, just changed existing ones
            newSDRF.getLayout().calculateLocations(newSDRF);
            sdrfWriter.write(newSDRF);

            getLog().debug("\n\n\n============================\n\n\n");
            getLog().info("IDF and SDRF files for " + investigation.getAccession() + " written to " + new File("").getAbsoluteFile().getParentFile().getAbsolutePath());
            log.debug("\n\n\n============================\n\n\n");
            return true;

        } catch (IOException | ParseException e) {
            e.printStackTrace();  //todo
            log.debug("\n\n\n============================\n\n\n");
            return false;
        }


    }

    private void initiatingCaches() {
        log.debug("Initiating caches");
        cacheOfZoomaResultsApplied = new HashMap<String, AnnotationSummary>();
        cacheOfLegacyAnnotations = new HashSet<String>();
        cacheOfItemsWithNoResults = new HashSet<String>();
        cacheOfItemsRequiringCuration = new HashMap<>();
        cacheOfExclusionsApplied = new HashMap<>();
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

        log.info("Processing " + sourceNodes.size() + " Source Nodes ...");


        for (SourceNode sourceNode : sourceNodes) {

            getLog().debug("Processing source node: " + sourceNode.getNodeName());


            for (CharacteristicsAttribute attribute : sourceNode.characteristics) {
                process(attribute);
            }

            // if we should add comments to the SDRF file
            if (addCommentsToSDRF) {

                sourceNode.comments.put("Zoomifications", comments);
                // reset the comments cache
                comments = new ArrayList<String>();
            }
        }


        // do the same for sampleNodes
        Collection<SampleNode> sampleNodes = sdrf.getNodes(SampleNode.class);

        log.info("Processing " + sampleNodes.size() + " Sample Nodes ...");

        for (SampleNode sampleNode : sampleNodes) {
            getLog().debug("Processing sample node: " + sampleNode.getNodeName());

            for (CharacteristicsAttribute attribute : sampleNode.characteristics) {
                process(attribute);
            }

            // if we should add comments to the SDRF file
            if (addCommentsToSDRF) {

                sampleNode.comments.put("Zoomifications", comments);
                // reset the comments cache
                comments = new ArrayList<String>();
            }
        }

        // do the same for hybridizationNodes
        Collection<HybridizationNode> hybridizationNodes = sdrf.getNodes(HybridizationNode.class);

        log.info("Processing " + hybridizationNodes.size() + " Hybridization Nodes ...");


        for (HybridizationNode hybridizationNode : hybridizationNodes) {
            getLog().debug("Processing hybridization node: " + hybridizationNode.getNodeName());

            for (FactorValueAttribute attribute : hybridizationNode.factorValues) {
                process(attribute);
            }

            // if we should add comments to the SDRF file
            if (addCommentsToSDRF) {

                hybridizationNode.comments.put("Zoomifications", comments);
                // reset the comments cache
                comments = new ArrayList<String>();
            }
        }

        return sdrf;

    }

    private CharacteristicsAttribute process(CharacteristicsAttribute charAttribute) {

        // First create the baseline transitional attribute
        TransitionalAttribute transAttribute = new TransitionalAttribute(magetabAccession, charAttribute);

        getLog().debug("");

//        check zooma cache
//        if found, apply zoomifications and continue to next attribute

        String input = transAttribute.getType() + ":" + transAttribute.getOriginalValue();

        // check zooma results cache
        AnnotationSummary cachedZoomaAnnotation = cacheOfZoomaResultsApplied.get(input);

        // if there is a corresponding cached Zooma annotation
        if (cachedZoomaAnnotation != null) {

            log.debug(input + ": Retrieved result from Zooma cache.");

            // store it in the transitional attribute
            transAttribute = storeZoomifications(transAttribute, cachedZoomaAnnotation);

            // then apply the changes to the characteristicsAttribute
            charAttribute = applyZoomifications(transAttribute, charAttribute);

            return charAttribute;
        }

        // if not found in results cache... continue to curation cache check
        if (cacheOfItemsRequiringCuration.get(input) != null) {

            log.debug(input + ": Retrieved result from cache of items requiring curation. Skipping Zoomifications.");

            // if found, item requires curation, so return the charAttribute without making any changes
            return charAttribute;
        }

        // if not in the results cache or the curation cache, check exclusions cache
        if (cacheOfExclusionsApplied.get(transAttribute.getFields()) != null) {

            log.debug(input + ": Retrieved result from cache of items to exclude. Skipping Zoomifications.");

            // if found, item should be excluded, so return the charAttribute without making any changes
            return charAttribute;
        }

        // Otherwise, we are seeing this input for the first time, so proceed ...
        log.debug("Processing '" + input + "'");

        // Check exclusions
        boolean exclude = checkAllExclusionProfiles(transAttribute);

        // If not excluded ...
        if (!exclude) {

            // then get the corresponding zooma annotations summary, if any
            AnnotationSummary zoomaAnnotationSummary = getZoomaAnnotationSummary(transAttribute);

            // if there is a corresponding annotation
            if (zoomaAnnotationSummary != null) {

                // store it in the transitional attribute
                transAttribute = storeZoomifications(transAttribute, zoomaAnnotationSummary);

                // then apply the changes to the characteristicsAttribute
                charAttribute = applyZoomifications(transAttribute, charAttribute);
            } else cacheOfItemsWithNoResults.add(input);
        }

        // return the charAttribute whether or not it has been modified
        return charAttribute;
    }

//    private CharacteristicsAttribute applyZoomifications(AnnotationSummary zoomaAnnotationSummary,CharacteristicsAttribute characteristicsAttribute){
//
//        TransitionalAttribute transitionalAttribute =
//
//        // if there is a corresponding annotation
//        if (zoomaAnnotationSummary != null) {
//
//            // store it in the transitional attribute
//            transAttribute = storeZoomifications(transAttribute, zoomaAnnotationSummary);
//
//            // then apply the changes to the characteristicsAttribute
//            charAttribute = applyZoomifications(transAttribute, charAttribute);
//        }
//    }


    private FactorValueAttribute process(FactorValueAttribute factorValueAttribute) {
        // First create the baseline transitional attribute
        TransitionalAttribute transAttribute = new TransitionalAttribute(magetabAccession, factorValueAttribute);

        getLog().debug("");

        log.debug("Processing " + transAttribute.getType() + ":" + transAttribute.getOriginalValue());

        // Check exclusions
        boolean exclude = checkAllExclusionProfiles(transAttribute);

        // If not excluded, get annotations

        if (!exclude) {
            // then get the corresponding zooma annotations summary, if any
            AnnotationSummary zoomaAnnotationSummary = getZoomaAnnotationSummary(transAttribute);

            // if there is a corresponding annotation, store it in the transitional attribute
            if (zoomaAnnotationSummary != null) {
                transAttribute = storeZoomifications(transAttribute, zoomaAnnotationSummary);

                // then apply the changes to the characteristicsAttribute
                factorValueAttribute = applyZoomifications(transAttribute, factorValueAttribute);
            }
        }

        // return the factorValueAttribute whether or not it has been modified
        return factorValueAttribute;
    }

    private CharacteristicsAttribute applyZoomifications(TransitionalAttribute transAttribute, CharacteristicsAttribute charAttribute) {

        // if we should overwrite the term source value, do so
        if (overwriteValues) {
            getLog().warn("Overwriting " + charAttribute.getAttributeValue() + " with " + transAttribute.getZoomifiedOntologyLabel());
            charAttribute.setAttributeValue(transAttribute.getZoomifiedOntologyLabel());
        }

        //log existing legacy annotations if there are any.
        if (charAttribute.termAccessionNumber != null && !charAttribute.termAccessionNumber.equals("")) {
            cacheOfLegacyAnnotations.add(
                    charAttribute.type + logFileDelimiter +
                            charAttribute.getAttributeValue() + logFileDelimiter +
                            //zoomified value not applicable
                            "" + logFileDelimiter +
                            //ont label not applicable
                            "" + logFileDelimiter +
                            charAttribute.termSourceREF + logFileDelimiter +
                            charAttribute.termAccessionNumber);

        }

        // if we should overwrite annotations, or if annotations are missing, apply zoomified annotations
        if (overwriteAnnotations || charAttribute.termSourceREF == null || charAttribute.termSourceREF.equals("")) {

            // todo: check... IF the zoomified annotation is null, then this update effectively strips existing annotations
            charAttribute.termSourceREF = transAttribute.getZoomifiedTermSourceREF();
            charAttribute.termAccessionNumber = transAttribute.getZoomifiedTermAccessionNumber();
        }

        return charAttribute;

    }

    private FactorValueAttribute applyZoomifications(TransitionalAttribute transAttribute, FactorValueAttribute valueAttribute) {

        // if we should overwrite the attribute value, do so
        if (overwriteValues) {
            getLog().warn("Overwriting " + valueAttribute.getAttributeValue() + " with ontology label " + transAttribute.getZoomifiedOntologyLabel());
            valueAttribute.setAttributeValue(transAttribute.getZoomifiedOntologyLabel());
        }

        // if we should overwrite annotations, or if annotations are missing, apply zoomified annotations
        if (overwriteAnnotations || valueAttribute.termSourceREF == null || valueAttribute.termSourceREF.equals("")) {

            // todo: check... IF the zoomified annotation is null, then this update effectively strips existing annotations
            valueAttribute.termSourceREF = transAttribute.getZoomifiedTermSourceREF();
            valueAttribute.termAccessionNumber = transAttribute.getZoomifiedTermAccessionNumber();
        }

        return valueAttribute;
    }

    private boolean checkAllExclusionProfiles(TransitionalAttribute transAttribute) {


        boolean exclude = false;

        // Check the cache of exclusions applied
        if (cacheOfExclusionsApplied.get(transAttribute.getFields()) != null) {
            log.debug("Retrieved " + Arrays.toString(transAttribute.getFields()) + " from exclusions cache.");
            return true;
        }

        // If it has not previously been excluded, check it
        if (transAttribute.getOriginalValue().length() < minStringLength) {

            log.debug("'" + transAttribute.getOriginalValue() + "' does not meet the specified length requirement of " + minStringLength + " characters and will not be zoomified.");

            String[] attributeFields = transAttribute.getFields();
            boolean[] basisForExclusion = {false, true, false, false, false, false, false};
            buildExclusionsLog(attributeFields, basisForExclusion);

            return true;
        }

        for (String exclusionProfileString : exclusionProfiles) {
            if (exclusionProfileString != null && !exclusionProfileString.startsWith("#") && !exclusionProfileString.equals("")) {
                TransitionalAttribute exclusionProfile = new TransitionalAttribute(exclusionProfileString, logFileDelimiter);
                if (checkIndividualExclusionProfile(exclusionProfile, transAttribute)) {
                    exclude = true;
                    return exclude;
                }
            }
        }

        return exclude;
    }


    private boolean checkIndividualExclusionProfile(TransitionalAttribute exclusionProfile, TransitionalAttribute transAttribute) {
        boolean[] basisForExclusion = new boolean[7];

        // initialise exclusion flag
        boolean exclude = false;

        String[] exclusionFields = exclusionProfile.getFields();

//        // if exclusions can only be assessed on the zoomified product
//        for (int i = 2; i < 6; i++) {
//            if (exclusionFields[i] != null) {
//                AnnotationSummary summary = getZoomaAnnotationSummary(transAttribute);
//                if (summary != null) transAttribute = storeZoomifications(transAttribute, summary);
//            }
//        }

        String[] attributeFields = transAttribute.getFields();

        for (int i = 0; i < exclusionFields.length; i++) {
            // if there is a specified exclusion
            if (!exclusionFields[i].equals(""))
                if (attributeFields[i] != null && exclusionFields[i].equalsIgnoreCase(attributeFields[i])) {
                    basisForExclusion[i] = true;
                    exclude = true;
                } else {
                    exclude = false;
                    return exclude;
                }
        }

        if (exclude) {
            String logLine = buildExclusionsLog(attributeFields, basisForExclusion);
            getLog().debug("Exclusion on the basis of: " + logLine);
        }

        return exclude;
    }

    private String buildExclusionsLog(String[] attributeFields, boolean[] basisForExclusion) {
        String exclusionLog = "";

        for (int i = 0; i < basisForExclusion.length; i++) {
            exclusionLog += attributeFields[i];
            if (basisForExclusion[i])
                exclusionLog += "*"; //todo: parameterise delimiter
            exclusionLog += ",";
        }

        String attributeFieldsAsString = "";
        for (String field : attributeFields) {
            field = stripDelimsFromField(field, logFileDelimiter);
            attributeFieldsAsString += field + logFileDelimiter;
        }

        cacheOfExclusionsApplied.put(attributeFieldsAsString, basisForExclusion);
        return exclusionLog;
    }

    private String stripDelimsFromField(String field, String logFileDelimiter) {
        if (field != null && !field.equals("") && field.contains(logFileDelimiter)) {
            return field.replaceAll(logFileDelimiter, " ");
        } else return field;
    }

    private void appendComment(String varName, String oldString, String newString) {

//        if (buildComments) {
        // if there's no new string, just return
        if (newString == null || newString.equals("")) return;

        // else, initialize comment
        String comment = "";

        // if there's no original annotation, phrase the comment accordingly
        if (oldString == null || oldString.equals("")) comment = (varName + " set to " + newString + ".");

//            // otherwise if zoomification overwrites an existing annotation, phrase the comment accordingly
//        else if (!oldString.equalsIgnoreCase(newString))
//            comment = (varName + " " + this.type + " changed to " + type + ".");

        getLog().debug(comment);

        // finally, append the comment
        comments.add(comment);
//        } else getLog().error("The 'appendComment method' was probably invoked in error.");
    }

    /**
     * Sends request to Limpopo retrieves json for 'accession'
     *
     * @param accession MAGETAB accession
     * @return String in json format
     */
    public String fetchMAGETABFromWebservice(String accession) {
        // send a request to limpopo
        String getURL = limpopoPath + "/api/accessions/" + accession;
        HttpGet httpget = new HttpGet(getURL);
        String responseBody = "";

        DefaultHttpRequestRetryHandler defaultHttpRequestRetryHandler = new DefaultHttpRequestRetryHandler(3, false); //todo

        BasicHttpParams params = new BasicHttpParams();

        params.setParameter("format", "json");
        try {
            httpget.setParams(params);
            HttpResponse response = httpClient.execute(httpget);
            responseBody = EntityUtils.toString(response.getEntity());
            getLog().debug("runURL", "response " + responseBody); //prints the complete HTML code of the web-page
        } catch (Exception e) {
            e.printStackTrace();
        }
//        get.setHttpRequestRetryHandler(retryHandler);
//        get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//                defaultHttpRequestRetryHandler);

        // execute the method method to retrieve json for 'accession'
        return executeGet(httpget);
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
     * Takes an HttpGet and returns the corresponding message body as a string
     *
     * @param get
     * @return message body as a string
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
//                    log.info("This request generated a valid response with application/json response type");
                    return true;
                }
            }
        }
        throw new RuntimeException("Unexpected response type: should be application/json");
    }

    public void setExclusionProfiles(HashSet<String> exclusionProfiles) {
        this.exclusionProfiles = exclusionProfiles;
    }

    public HashSet<String> getCacheOfLegacyAnnotations() {

        return cacheOfLegacyAnnotations;
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


    /**
     * Delegates acquisition of a ZoomaAnnotationSummary based on type and value of the attribute passed in.
     * Using the resulting best summary, update the attribute accordingly.
     *
     * @param attribute original (unzoomified) TransitionalAttribute
     * @return zoomified attribute.
     */
    public TransitionalAttribute storeZoomifications(TransitionalAttribute attribute, AnnotationSummary zoomaAnnotationSummary) {
        getLog().debug("Storing zoomifications in transitional attribute");

        // if there are zooma results, store them in the transitional attribute
        if (zoomaAnnotationSummary != null) {

            attribute.setType(zoomaAnnotationSummary.getAnnotatedPropertyType());

            // note that this doesn't overwrite the ORIGINAL property value, just stores the zoomified one
            attribute.setZoomifiedValue(zoomaAnnotationSummary.getAnnotatedPropertyValue());

            ArrayList<String> refAndAccession = ZoomageTextUtils.concatenateCompoundURIs(zoomaAnnotationSummary, olsShortIds, compoundAnnotationDelimiter);

            // note that this doesn't overwrite the ORIGINAL Term Source Ref, but just stores the Zooma one
            attribute.setZoomifiedTermSourceREF(refAndAccession.get(0));

            // note that this doesn't overwrite the ORIGINAL accession, but just stores the Zooma one
            attribute.setZoomifiedTermAccessionNumber(refAndAccession.get(1));

            try {
                URI uri = zoomaAnnotationSummary.getSemanticTags().iterator().next();
                String ontLabel = zoomaClient.getLabel(uri);
                attribute.setZoomifiedOntologyLabel(ontLabel);
            } catch (IOException e) {
                log.error("Could not set ontology label for transitional attribute '" + attribute.getOriginalValue());
                e.printStackTrace();
            }
        }

        // return modified attribute
        return attribute;
    }

    public AnnotationSummary getZoomaAnnotationSummary(TransitionalAttribute attribute) {

        // clean and check the attribute type
        String normalisedType = ZoomageTextUtils.normaliseType(attribute.getType());
        return getZoomaAnnotationSummary(normalisedType, attribute.getOriginalValue());

    }


    private AnnotationSummary getZoomaAnnotationSummary(String cleanedAttributeType, String originalAttributeValue) {

        AnnotationSummary annotationSummary = null;

        String input = cleanedAttributeType + ":" + originalAttributeValue;

        // if there's a result in the cache of items requiring curation, return null
        if (cacheOfItemsRequiringCuration.containsKey(input)) {
            getLog().debug(input + " requires curation");
        }

        // if there's a result in the cache, fetch it
        else if (cacheOfZoomaResultsApplied.containsKey(input)) {
            if (cacheOfZoomaResultsApplied.get(input) != null) {
                getLog().debug("Fetching Zooma result from cache for \"" + input + "\"");
            } else getLog().debug("No Zooma result for \"" + input + "\"");

            annotationSummary = cacheOfZoomaResultsApplied.get(input);
        }

        // if there's no result in cache and it hasn't been previously determined as needing curation
        // initiate a new query
        else annotationSummary = initiateZoomaQueryAndFilterResults(cleanedAttributeType, originalAttributeValue);

        return annotationSummary;
    }

    private AnnotationSummary initiateZoomaQueryAndFilterResults(String cleanedAttributeType, String originalAttributeValue) {

        AnnotationSummary annotationSummary = null;

        String input = cleanedAttributeType + ":" + originalAttributeValue;
        // if there's no result in the results cache or curation cache, initiate a new Zooma query
        getLog().debug("Initiating new zooma query for '" + input + "'");


        Map<AnnotationSummary, Float> resultSetBeforeFilters = getUnfilteredZoomaResults(cleanedAttributeType, originalAttributeValue);


        if (resultSetBeforeFilters == null || resultSetBeforeFilters.size() == 0) {
            annotationSummary = null;
            cacheOfItemsWithNoResults.add(input);
        }

        // filter results based on cutoffpercentage specified by the user
        else {
            getLog().debug("Filtering " + resultSetBeforeFilters.size() + " Zooma result(s)");
            Set<AnnotationSummary> resultSetAfterFilters = ZoomaUtils.filterAnnotationSummaries(resultSetBeforeFilters, cutoffScore, cutoffPercentage);

            int numberOfResultsAfterFilters = resultSetAfterFilters.size();

            //  if there are no results after applying filters
            if (numberOfResultsAfterFilters == 0) {
                log.debug("None of the " + resultSetBeforeFilters.size() + " annotations meet the threshold for autocuration of " + input);
                annotationSummary = null;
                cacheOfItemsRequiringCuration.put(input, resultSetBeforeFilters.size());
            }

            // if there is one and only one result
            else if (numberOfResultsAfterFilters == 1) {
                annotationSummary = resultSetAfterFilters.iterator().next();
                cacheOfZoomaResultsApplied.put(input, annotationSummary);
            }

            // if more than one result for the given percentage and score params, don't automate the annotation
            else if (numberOfResultsAfterFilters > 1) {
                getLog().warn("More than one filtered result meets user criteria; no automatic curation applied to " + input);
                // For performance considerations, still put null in the cache for this input
                annotationSummary = null;
                cacheOfItemsRequiringCuration.put(input, numberOfResultsAfterFilters);

                // from among filtered results, get the best one and return it return getBestMatch(input, resultSetAfterFilters);
                // todo: this would be a prompt-the-user feature, not an automated curation feature
            }

        }

        return annotationSummary;

    }

    private Map<AnnotationSummary, Float> getUnfilteredZoomaResults(String cleanedAttributeType, String originalAttributeValue) {

        Property property = new SimpleTypedProperty(cleanedAttributeType, originalAttributeValue);

        Map<AnnotationSummary, Float> fullResultsMap = null;
        try {
            fullResultsMap = zoomaClient.searchZOOMA(property, 0);
        } catch (Exception e) {
            getLog().warn("Due to a Zooma error, no annotation summary could be fetched for " + cleanedAttributeType + ":" + originalAttributeValue);
            cacheOfItemsWithNoResults.add(cleanedAttributeType + ":" + originalAttributeValue);
//            e.printStackTrace();
        }
        return fullResultsMap;
    }


    public ArrayList<String> getCacheOfExclusionsApplied() {
        Iterator iterator = cacheOfExclusionsApplied.entrySet().iterator();

        ArrayList<String> lines = new ArrayList<String>();

        while (iterator.hasNext()) {
            String line = "";

            try {
                Map.Entry pairs = (Map.Entry) iterator.next();
                String attributeFieldsStr = (String) pairs.getKey();
                boolean[] basisForExclusion = cacheOfExclusionsApplied.get(attributeFieldsStr);

                String[] attributeFields = attributeFieldsStr.split(logFileDelimiter);

                for (int i = 0; i < attributeFields.length; i++) {
                    line += attributeFields[i];
                    if (basisForExclusion[i]) line += "*";
                    line += logFileDelimiter;
                }

                lines.add(line);
                iterator.remove(); // avoids a ConcurrentModificationException
            } catch (EmptyStackException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Collections.sort(lines);

        return lines;
    }


    public ArrayList<String> getCacheOfZoomificationsApplied() throws IOException {

        Iterator iterator = cacheOfZoomaResultsApplied.entrySet().iterator();

        ArrayList<String> lines = new ArrayList<String>();
        String line = "";
        while (iterator.hasNext()) {

            try {
                Map.Entry pairs = (Map.Entry) iterator.next();
                String input = (String) pairs.getKey();

                AnnotationSummary zoomaAnnotationSummary = cacheOfZoomaResultsApplied.get(input);

                input = input.replace(":", logFileDelimiter);

                //ORIGINAL TYPE & ORIGINAL VALUE
                line = input;

                if (zoomaAnnotationSummary == null) {
                    getLog().debug(input + " has no corresponding annotation.");
                    line += logFileDelimiter + "none" + logFileDelimiter + "none" + logFileDelimiter + "none" + logFileDelimiter + "none" + logFileDelimiter + "none" + magetabAccession;
                    lines.add(line);
                } else {
                    String zoomagedValue = zoomaAnnotationSummary.getAnnotatedPropertyValue();
                    ArrayList<String> refAndAcession = ZoomageTextUtils.concatenateCompoundURIs(zoomaAnnotationSummary, olsShortIds, compoundAnnotationDelimiter);
                    String termSourceRef = refAndAcession.get(0);
                    String termSourceAccession = refAndAcession.get(1);

                    String originalValue = input.substring(input.indexOf(logFileDelimiter) + 1);

                    //ZOOMA VALUE
                    if (!zoomagedValue.equalsIgnoreCase(originalValue)) {
                        line += logFileDelimiter + zoomagedValue;
                    } else line += logFileDelimiter + "~";

                    //ONT LABEL
                    String ontLabel = "";
                    URI uri = zoomaAnnotationSummary.getSemanticTags().iterator().next();

                    try {
                        ZOOMASearchClient zoomaClient = new ZOOMASearchClient(URI.create(zoomaPath).toURL());
                        ontLabel = zoomaClient.getLabel(uri);
                        if (!zoomagedValue.equalsIgnoreCase(ontLabel)) {
                            line += logFileDelimiter + ontLabel;
                        } else line += logFileDelimiter + "~";
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Failed to create ZOOMASearchCLient", e);
                    } catch (Exception e) {
                        line += logFileDelimiter + "Ontology label could not be fetched";
                        log.error("Unable to get ontology label from zoomaRESTClient.getClient().getLabel(" + uri + ").");
//                        e.printStackTrace();
                    }

                    // TERM SOURCE REF & TERM SOURCE ACCESSION
                    line += logFileDelimiter + termSourceRef + logFileDelimiter + termSourceAccession + logFileDelimiter + magetabAccession;

                    lines.add(line);

                }

                iterator.remove(); // avoids a ConcurrentModificationException
            } catch (EmptyStackException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.sort(lines);
        return lines;
    }


    public ArrayList<String> getCacheOfItemsRequiringCuration() {
        Iterator iterator = cacheOfItemsRequiringCuration.entrySet().iterator();

        ArrayList<String> lines = new ArrayList<String>();
        String line = "";

        while (iterator.hasNext()) {

            try {
                Map.Entry pairs = (Map.Entry) iterator.next();
                String input = (String) pairs.getKey();

                Integer numberOfResults = cacheOfItemsRequiringCuration.get(input);

                input = input.replace(":", logFileDelimiter);

                //ORIGINAL TYPE & ORIGINAL VALUE
                line = input;

                //SKIP OTHER FIELDS
                line += logFileDelimiter + logFileDelimiter + logFileDelimiter + logFileDelimiter + logFileDelimiter + magetabAccession + logFileDelimiter + numberOfResults;

                lines.add(line);

                iterator.remove(); // avoids a ConcurrentModificationException
            } catch (EmptyStackException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Collections.sort(lines);

        return lines;

    }

    public ArrayList<String> getCacheOfItemsWithNoResults() {

        ArrayList<String> lines = new ArrayList<String>();
        String line = "";

        for (String input : cacheOfItemsWithNoResults) {
            input = input.replace(":", logFileDelimiter);

            //ORIGINAL TYPE & ORIGINAL VALUE
            line = input;

            //SKIP OTHER FIELDS
            line += logFileDelimiter + logFileDelimiter + logFileDelimiter + logFileDelimiter + logFileDelimiter + magetabAccession + logFileDelimiter + 0;

            lines.add(line);

        }

        Collections.sort(lines);

        return lines;

    }


}