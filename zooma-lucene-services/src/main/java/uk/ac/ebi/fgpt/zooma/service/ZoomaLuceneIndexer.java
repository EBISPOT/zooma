package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationSummaryDAO;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Constructs a search index of annotations and properties using a Lucene implementation.  This enables fast text-based
 * searches over the documents contained within the index.
 *
 * @author Tony Burdett
 * @date 02/04/12
 */
public class ZoomaLuceneIndexer extends Initializable {
    public static final Version VERSION = Version.LUCENE_5_3_0;

    private static final String ENCODING = "SHA-1";
    private static final String HEX_CHARACTERS = "0123456789ABCDEF";

    // lucene analyzer for producing indexed strings
    private Analyzer analyzer;

    // maximum number of entities that will be fetched from DAO, usually only used in testing
    private int maxEntityCount = -1;

    // DAOs to fetch entities which will be used to build indices
    private AnnotationDAO annotationDAO;
    private AnnotationSummaryDAO annotationSummaryDAO;

    private PropertyDAO propertyDAO;

    // index directories
    private Directory propertyIndex;
    private Directory propertyTypeIndex;
    private Directory annotationCountIndex;
    private Directory annotationIndex;
    private Directory annotationSummaryIndex;
    private Map<URI, Collection<URI>> propertyUriToSourcesMap = new HashMap<>();
    private Map<String, Collection<URI>> propertyTypeToSourcesMap = new HashMap<>();


    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public int getMaxEntityCount() {
        return maxEntityCount;
    }

    public void setMaxEntityCount(int maxEntityCount) {
        this.maxEntityCount = maxEntityCount;
    }

    public AnnotationDAO getAnnotationDAO() {
        return annotationDAO;
    }

    public void setAnnotationDAO(AnnotationDAO annotationDAO) {
        this.annotationDAO = annotationDAO;
    }

    public PropertyDAO getPropertyDAO() {
        return propertyDAO;
    }

    public void setPropertyDAO(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    public Directory getPropertyIndex() {
        return propertyIndex;
    }

    public void setPropertyIndex(Directory propertyIndex) {
        this.propertyIndex = propertyIndex;
    }

    public Directory getPropertyTypeIndex() {
        return propertyTypeIndex;
    }

    public void setPropertyTypeIndex(Directory propertyTypeIndex) {
        this.propertyTypeIndex = propertyTypeIndex;
    }

    public Directory getAnnotationCountIndex() {
        return annotationCountIndex;
    }

    public void setAnnotationCountIndex(Directory annotationCountIndex) {
        this.annotationCountIndex = annotationCountIndex;
    }

    public Directory getAnnotationIndex() {
        return annotationIndex;
    }

    public void setAnnotationIndex(Directory annotationIndex) {
        this.annotationIndex = annotationIndex;
    }

    public Directory getAnnotationSummaryIndex() {
        return annotationSummaryIndex;
    }

    public void setAnnotationSummaryIndex(Directory annotationSummaryIndex) {
        this.annotationSummaryIndex = annotationSummaryIndex;
    }

    public AnnotationSummaryDAO getAnnotationSummaryDAO() {
        return annotationSummaryDAO;
    }

    public void setAnnotationSummaryDAO(AnnotationSummaryDAO annotationSummaryDAO) {
        this.annotationSummaryDAO = annotationSummaryDAO;
    }

    /**
     * Returns a flag to indicate whether initialization of this indexer has already been successful or not
     *
     * @return true if this is initialized, false otherwise.
     * @throws IllegalStateException if initialization failed
     */
    public boolean isInitialized() {
        return isReady();
    }

    public void createPropertyIndices(Collection<Property> properties) throws IOException {
        getLog().debug("Creating lucene indices from " + properties.size() + " properties...");

        // collect unique property types
        Set<String> uniquePropertyTypes = new HashSet<>();

        // set up index
        IndexWriterConfig propertyConfig = new IndexWriterConfig(getAnalyzer());
        IndexWriter propertyIndexWriter = new IndexWriter(getPropertyIndex(), propertyConfig);
        IndexWriterConfig propertyTypeConfig = new IndexWriterConfig(getAnalyzer());
        IndexWriter propertyTypeIndexWriter = new IndexWriter(getPropertyTypeIndex(), propertyTypeConfig);

        // iterate over all properties
        for (Property property : properties) {
            // build the document to index text data and store URIs
            Document doc = new Document();

            doc.add(new Field("uri",
                    property.getURI().toString(),
                    Field.Store.YES,
                    Field.Index.NOT_ANALYZED));
            doc.add(new Field("name",
                    property.getPropertyValue(),
                    Field.Store.YES,
                    Field.Index.ANALYZED));
            if (property instanceof TypedProperty) {
                String propertyType = ((TypedProperty) property).getPropertyType();

                // add type field to property index
                doc.add(new Field("type",
                        propertyType,
                        Field.Store.YES,
                        Field.Index.ANALYZED));


                // add type document to property type index if not a duplicate
                if (!uniquePropertyTypes.contains(propertyType)) {
                    // this is a new property type, add to unique set
                    uniquePropertyTypes.add(propertyType);

                    // and index
                    Document typeDoc = new Document();
                    typeDoc.add(new Field("name",
                            propertyType,
                            Field.Store.YES,
                            Field.Index.ANALYZED));
                    // add any sources where this property type is used
                    for (URI sourceUri : propertyTypeToSourcesMap.get(propertyType)) {
                        typeDoc.add(new Field("source",
                                sourceUri.toString(),
                                Field.Store.YES,
                                Field.Index.NOT_ANALYZED));
                    }
                    propertyTypeIndexWriter.addDocument(typeDoc);
                }
            }

            // add any sources where this property is used
            for (URI sourceUri : propertyUriToSourcesMap.get(property.getURI())) {
                doc.add(new Field("source",
                        sourceUri.toString(),
                        Field.Store.YES,
                        Field.Index.NOT_ANALYZED));
            }

            // add this document to the index
            propertyIndexWriter.addDocument(doc);
        }

        // now we have indexed all properties, close the index writer
        propertyIndexWriter.close();
        propertyTypeIndexWriter.close();
        getLog().debug("Property lucene indexing complete!");
    }

    public void clearPropertyIndices() {
    }

    public void createAnnotationCountIndex(int size) throws IOException {
        getLog().debug("Creating annotation count lucene index...");

        // build the document to index total count of annotations
        Document doc = new Document();
        doc.add(new Field("count",
                Integer.toString(size),
                Field.Store.YES,
                Field.Index.ANALYZED));

        IndexWriter indexWriter = obtainIndexWriter(getAnnotationCountIndex());
        indexWriter.addDocument(doc);
        indexWriter.close();
        getLog().debug("Annotation count lucene indexing complete!");
    }

    public void clearAnnotationCountIndex() {

    }

    public Map<URI, AnnotationProvenance> createAnnotationIndex(List<Annotation> annotations) throws IOException {
        getLog().info("Creating lucene index for " + annotations.size() + " annotations");
        ConcurrentHashMap<URI, AnnotationProvenance> provenanceMap = new ConcurrentHashMap<>();

        IndexWriter annotationIndexWriter = obtainIndexWriter(getAnnotationIndex());

        createAnnotationIndex(annotations, provenanceMap, annotationIndexWriter);

        // now we have indexed all annotations, close the index writer
        annotationIndexWriter.close();

        getLog().debug("Annotation lucene indexing complete!");
        return provenanceMap;
    }

    public void createAnnotationIndex(Collection<Annotation> annotations,
                                      Map<URI, AnnotationProvenance> provenanceMap,
                                      IndexWriter indexWriter) throws IOException {
        getLog().debug("Creating lucene index from " + annotations.size() + " annotations...");

        // iterate over all annotations
        for (Annotation annotation : annotations) {
            Property property = annotation.getAnnotatedProperty();

            // build the document to index text data and store URIs
            Document doc = new Document();
            doc.add(new Field("uri",
                    annotation.getURI().toString(),
                    Field.Store.YES,
                    Field.Index.NOT_ANALYZED));
            doc.add(new Field("property",
                    property.getPropertyValue(),
                    Field.Store.YES,
                    Field.Index.ANALYZED));
            if (property instanceof TypedProperty) {
                String propertyType = ((TypedProperty) property).getPropertyType();
                doc.add(new Field("propertytype",
                        propertyType,
                        Field.Store.YES,
                        Field.Index.ANALYZED));

                // keep a map of types to sources
                if (!propertyTypeToSourcesMap.containsKey(propertyType)) {
                    propertyTypeToSourcesMap.put(propertyType, new HashSet<URI>());
                }
                propertyTypeToSourcesMap.get(propertyType).add(annotation.getProvenance().getSource().getURI());

            }
            if (annotation.getProvenance() != null && annotation.getProvenance().getSource() != null) {
                doc.add(new Field("source",
                        annotation.getProvenance().getSource().getURI().toString(),
                        Field.Store.YES,
                        Field.Index.NOT_ANALYZED));
            }
            for (URI target : annotation.getSemanticTags()) {
                // allow for null semantic tags -
                // "null" is a positive assertion of NO mapping, but shouldn't be indexed (not searchable)
                if (target != null) {
                    doc.add(new Field("target",
                            target.toString(),
                            Field.Store.YES,
                            Field.Index.NOT_ANALYZED));
                }
            }

            provenanceMap.put(annotation.getURI(), annotation.getProvenance());
            doc.add(new Field("quality",
                    Float.toString(scoreAnnotationQuality(annotation.getProvenance())),
                    Field.Store.YES,
                    Field.Index.ANALYZED));




            // we want to keep a map of property uris to sources for the property index
            if (!propertyUriToSourcesMap.containsKey(property.getURI())) {
                propertyUriToSourcesMap.put(property.getURI(), new HashSet<URI>());
            }
            propertyUriToSourcesMap.get(property.getURI()).add(annotation.getProvenance().getSource().getURI());
            // add this document to the index
            indexWriter.addDocument(doc);
        }
    }

    public void clearAnnotationIndex() {

    }

    public void createAnnotationSummaryIndex(AnnotationSummaryDAO summaryDao,
                                             Map<URI, AnnotationProvenance> provenanceMap) throws IOException {

        getLog().info("Creating annotation summary lucene index...");

        IndexWriter summaryIndexWriter = obtainIndexWriter(getAnnotationSummaryIndex());

        final String UNTYPED = "##zooma.untyped.property.key##";

        Collection<AnnotationSummary> summaries = summaryDao.read();
        getLog().debug("Number of summaries to index: " + summaries.size());

        Map<String, Float> summaryIdToMaxScore = new HashMap<>();
        Map<String, Set<URI>> summaryIdToSourcesMap = new HashMap<>();

        for (AnnotationSummary summary : summaries) {

            String summaryId = null;
            // get property
            String propertyType =
                    summary.getAnnotatedPropertyType() != null ? summary.getAnnotatedPropertyType() : UNTYPED;
            String propertyValue = summary.getAnnotatedPropertyValue();
            // get semantic tags
            URI propertyUri = summary.getAnnotatedPropertyUri();
            Collection<URI> semanticTags = summary.getSemanticTags();
            Collection<URI> annotations = summary.getAnnotationURIs();

            // generate summary id
            List<String> idContent = new ArrayList<>();
            for (URI uri : semanticTags) {
                if (uri != null) {
                    // append URI to ID
                    idContent.add(uri.toString());
                }
            }

            if (idContent.size() > 0) {
                // add property type and value to id content
                idContent.add(0, propertyValue);
                idContent.add(0, propertyType);

                summaryId = generateEncodedID(idContent.toArray(new String[idContent.size()]));
                getLog().trace("Found new unique combination - " +
                        "property value '" + propertyValue + "', type '" + propertyType + "' " +
                        "maps to " + semanticTags + " (" + summaryId + ")");

            }

            if (summaryId != null) {
                summaryIdToSourcesMap.put(summaryId, new HashSet<URI>());

                // check annotation score against current max - if no current max, or if greater, replace
                for (URI annoUri : annotations) {
                    if (!provenanceMap.containsKey(annoUri)) {
                        getLog().warn("No provenance for annotation " + annoUri.toString());
                    }
                    else {
                        AnnotationProvenance prov = provenanceMap.get(annoUri);
                        float annotationScore = scoreAnnotationQuality(prov);
                        if (!summaryIdToMaxScore.containsKey(summaryId) ||
                                (annotationScore > summaryIdToMaxScore.get(summaryId))) {
                            summaryIdToMaxScore.put(summaryId, scoreAnnotationQuality(prov));
                        }
                        summaryIdToSourcesMap.get(summaryId).add(prov.getSource().getURI());
                    }
                }

                // build one document to index each summary combination
                Document doc = new Document();
                doc.add(new Field("id",
                        summaryId,
                        Field.Store.YES,
                        Field.Index.NOT_ANALYZED));
                doc.add(new Field("propertyuri",
                        propertyUri.toString(),
                        Field.Store.YES,
                        Field.Index.NOT_ANALYZED));
                doc.add(new Field("property",
                        propertyValue,
                        Field.Store.YES,
                        Field.Index.ANALYZED));
                if (!propertyType.equals(UNTYPED)) {
                    doc.add(new Field("propertytype",
                            propertyType,
                            Field.Store.YES,
                            Field.Index.ANALYZED));
                }
                // add field for each semantic tag
                for (URI uri : semanticTags) {
                    if (uri != null) {
                        // add a field for this URI
                        getLog().trace("Next summary semantic tag: " + uri);
                        doc.add(new Field("semanticTag",
                                uri.toString(),
                                Field.Store.YES,
                                Field.Index.ANALYZED));
                    }
                }
                // add field for each annotation
                for (URI annotationUri : annotations) {
                    // add a field for this URI
                    getLog().trace("Next summary annotation: " + annotationUri.toString());
                    doc.add(new Field("annotation",
                            annotationUri.toString(),
                            Field.Store.YES,
                            Field.Index.ANALYZED));
                }
                // add a field for the frequency of use of this pattern
                getLog().trace("Summary frequency: " + annotations.size());
                doc.add(new Field("frequency",
                        Integer.toString(annotations.size()),
                        Field.Store.YES,
                        Field.Index.ANALYZED));
                getLog().trace("Best score: " + summaryIdToMaxScore.get(summaryId));
                doc.add(new Field("topScore",
                        Float.toString(summaryIdToMaxScore.get(summaryId)),
                        Field.Store.YES,
                        Field.Index.ANALYZED));
                for (URI source : summaryIdToSourcesMap.get(summaryId)) {
                    getLog().trace("Next source: " + source.toString());
                    doc.add(new Field("source",
                            source.toString(),
                            Field.Store.YES,
                            Field.Index.NOT_ANALYZED));

                }

                getLog().trace("Number of times verified: " + summaryIdToSourcesMap.get(summaryId).size());
                doc.add(new Field("timesVerified",
                        Integer.toString(summaryIdToSourcesMap.get(summaryId).size()),
                        Field.Store.YES,
                        Field.Index.ANALYZED));

                getLog().trace("Annotation Summary index entry:\n\t" +
                        "ID: " + summaryId + ",\n\t" +
                        "Property: " + propertyValue + ",\n\t" +
                        "Property Type: " + propertyType + ",\n\t" +
                        "Semantic Tags: " + semanticTags.toString() + ",\n\t" +
                        "Summary Frequency: " + annotations.size() + ",\n\t" +
                        "Best score: " + summaryIdToMaxScore.get(summaryId) + ",\n\t" +
                        "Times verified: " + summaryIdToSourcesMap.get(summaryId).size());

                // add this document to the index
                summaryIndexWriter.addDocument(doc);
            }

        }
        getLog().info("Annotation summary lucene indexing complete!");
        summaryIndexWriter.close();
    }

    public void clearAnnotationSummaryIndex() {

    }

    @Override
    protected void doInitialization() throws Exception {
        getLog().info("ZOOMA index directory: " + System.getProperty("zooma.data.dir"));
        getLog().info("Cleaning lucene indices...");
        clearAnnotationSummaryIndex();
        clearAnnotationIndex();
        clearAnnotationCountIndex();
        clearPropertyIndices();
        getLog().info("Querying underlying datasources for annotations to index...");
        Collection<Annotation> annotations = getAnnotationDAO().read();
        getLog().info("Total annotations:" + annotations.size());

        int count = getMaxEntityCount() == -1 ? annotations.size() : getMaxEntityCount();
        getLog().info("Total annotation to index:" + count);
        createAnnotationCountIndex(count);
        Map<URI, AnnotationProvenance> provenanceMap = createAnnotationIndex(new ArrayList<>(annotations));
        createAnnotationSummaryIndex(getAnnotationSummaryDAO(), provenanceMap);

        getLog().info("Querying underlying datasources for properties to index...");
        Collection<Property> properties = getMaxEntityCount() == -1
                ? getPropertyDAO().read()
                : getPropertyDAO().read(getMaxEntityCount(), 0);
        getLog().info("Building lucene indices...");
        createPropertyIndices(properties);

        getLog().info("Lucene indexing complete!");
    }

    @Override
    protected void doTermination() throws Exception {
        // clear all indices
        getLog().info("Cleaning lucene indices...");
        clearAnnotationSummaryIndex();
        clearAnnotationIndex();
        clearAnnotationCountIndex();
        clearPropertyIndices();

        // close all open resources
        getLog().info("Closing all open lucene indices...");
        propertyIndex.close();
        propertyTypeIndex.close();
        annotationCountIndex.close();
        annotationIndex.close();
        annotationSummaryIndex.close();
        analyzer.close();
        getLog().info(getClass().getSimpleName() + " shutdown OK.");
    }

    protected IndexWriter obtainIndexWriter(Directory directory) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(getAnalyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        return new IndexWriter(directory, config);
    }

    /**
     * Returns a float value that is the quality score for the given annotation.
     * <p/>
     * This score is evaluated by an algorithm that considers: <ul> <li>Source (e.g. Atlas, AE2, ZOOMA)</li>
     * <li>Evidence (Manually created, Inferred, etc.)</li> <li>Creator - Who made this annotation?</li> <li>Time of
     * creation - How recent is this annotation?</li> </ul>
     *
     * @param prov the annotation to score
     * @return the quality score attributed to this annotation
     */
    protected float scoreAnnotationQuality(AnnotationProvenance prov) {
        // evidence is most important factor, invert so ordinal 0 gets highest score
        int evidenceScore = AnnotationProvenance.Evidence.values().length - prov.getEvidence().ordinal();
        // creation time should then work backwards from most recent to oldest
        long age = prov.getGeneratedDate().getTime();

        float score = (float) (evidenceScore + Math.log10(age));
        getLog().trace("Evaluated annotation score as " + score);
        return score;
    }

    private Map<String, String[]> idKeyContentMap = Collections.synchronizedMap(new HashMap<String, String[]>());

    private String generateEncodedID(String... contents) {
        StringBuilder idContent = new StringBuilder();
        for (String s : contents) {
            idContent.append(s);
        }
        try {
            // encode the content using SHA-1
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(idContent.toString().getBytes("UTF-8"));

            // now translate the resulting byte array to hex
            String idKey = getHexRepresentation(digest);
            if (idKeyContentMap.containsKey(idKey)) {
                // key collision, check contents
                String[] collisionContents = idKeyContentMap.get(idKey);
                if (contents.length != collisionContents.length) {
                    // mismatched key content length, genuine key collision
                    throw new RuntimeException(
                            "Key collision (content length mismatch) trying to generate unique key for " + idContent);
                }
                else {
                    for (int i = 0; i < contents.length; i++) {
                        // mismatched key content element, genuine key collision
                        if (!contents[i].equals(collisionContents[i])) {
                            throw new RuntimeException(
                                    "Key collision (content element mismatch at " + i + ") " +
                                            "trying to generate unique key for " + idContent);
                        }
                    }
                }
            }
            else {
                idKeyContentMap.put(idKey, contents);
            }

            getLog().trace("Generated new " + ENCODING + " based, hex encoded ID string: " + idKey);
            return idKey;
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported!");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(ENCODING + " algorithm not available, this is required to generate ID");
        }
    }

    private String getHexRepresentation(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEX_CHARACTERS.charAt((b & 0xF0) >> 4)).append(HEX_CHARACTERS.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
