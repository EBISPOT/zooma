package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationProvenanceDAO;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationSummaryDAO;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.util.CollectionUtils;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Constructs a search index of annotations and properties using a Lucene implementation.  This enables fast text-based
 * searches over the documents contained within the index.
 *
 * @author Tony Burdett
 * @date 02/04/12
 */
public class ZoomaLuceneIndexer extends Initializable {
    public static final Version VERSION = Version.LUCENE_35;

    private static final String ENCODING = "SHA-1";
    private static final String HEX_CHARACTERS = "0123456789ABCDEF";

    // lucene analyzer for producing indexed strings
    private Analyzer analyzer;

    // maximum number of entities that will be fetched from DAO, usually only used in testing
    private int maxEntityCount = -1;

    private int threadCount = 1;

    private int annotationsPerThread = 100;


    // DAOs to fetch entities which will be used to build indices
    private AnnotationDAO annotationDAO;

    private AnnotationSummaryDAO annotationSummaryDAO;

    private AnnotationProvenanceDAO annotationProvenanceDAO;

    private PropertyDAO propertyDAO;

    // index directories
    private Directory propertyIndex;
    private Directory propertyTypeIndex;
    private Directory annotationCountIndex;
    private Directory annotationIndex;
    private Directory annotationSummaryIndex;

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

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getAnnotationsPerThread() {
        return annotationsPerThread;
    }

    public void setAnnotationsPerThread(int annotationsPerThread) {
        this.annotationsPerThread = annotationsPerThread;
    }

    public AnnotationProvenanceDAO getAnnotationProvenanceDAO() {
        return annotationProvenanceDAO;
    }

    public void setAnnotationProvenanceDAO(AnnotationProvenanceDAO annotationProvenanceDAO) {
        this.annotationProvenanceDAO = annotationProvenanceDAO;
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
        IndexWriterConfig propertyConfig = new IndexWriterConfig(VERSION, getAnalyzer());
        IndexWriter propertyIndexWriter = new IndexWriter(getPropertyIndex(), propertyConfig);
        IndexWriterConfig propertyTypeConfig = new IndexWriterConfig(VERSION, getAnalyzer());
        IndexWriter propertyTypeIndexWriter = new IndexWriter(getPropertyTypeIndex(), propertyTypeConfig);

        // iterate over all properties
        for (Property property : properties) {
            // build the document to index text data and store URIs
            Document doc = new Document();

            doc.add(new Field("uri",
                    property.getURI().toString(),
                    Field.Store.YES,
                    Field.Index.ANALYZED));
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
                    propertyTypeIndexWriter.addDocument(typeDoc, getAnalyzer());
                }
            }

            // add this document to the index
            propertyIndexWriter.addDocument(doc, getAnalyzer());
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

    public void createAnnotationIndex(List<Annotation> annotations) throws IOException {
        getLog().info("Creating threaded lucene index for annotations, with " + getThreadCount() + " threads");

        IndexWriter annotationIndexWriter = obtainIndexWriter(getAnnotationIndex());

        ExecutorService executor = Executors.newFixedThreadPool(getThreadCount());

        for (int x = 0; x <annotations.size(); x++) {
            int offset = x;

            int limit = offset + getAnnotationsPerThread();
            if (offset + getAnnotationsPerThread() > annotations.size()) {
                limit = annotations.size();
            }

            Runnable worker = new RunnableAnnotationIndexBuilder(this, annotationIndexWriter, annotations.subList(offset, limit));
            executor.execute(worker);
            x = x + getAnnotationsPerThread();
        }

        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        getLog().info("Finished all threads");

        // now we have indexed all annotations, close the index writer
        annotationIndexWriter.close();

        getLog().debug("Annotation lucene indexing complete!");
    }

    public void createAnnotationIndex(Collection<Annotation> annotations, IndexWriter indexWriter) throws IOException {
        getLog().debug("Creating lucene index from " + annotations.size() + " annotations...");

        // iterate over all annotations
        for (Annotation annotation : annotations) {
            Property property = annotation.getAnnotatedProperty();

            // build the document to index text data and store URIs
            Document doc = new Document();
            doc.add(new Field("uri",
                    annotation.getURI().toString(),
                    Field.Store.YES,
                    Field.Index.ANALYZED));
            doc.add(new Field("property",
                    property.getPropertyValue(),
                    Field.Store.YES,
                    Field.Index.ANALYZED));
            if (property instanceof TypedProperty) {
                doc.add(new Field("propertytype",
                        ((TypedProperty) property).getPropertyType(),
                        Field.Store.YES,
                        Field.Index.ANALYZED));
            }
            for (URI target : annotation.getSemanticTags()) {
                // allow for null semantic tags -
                // "null" is a positive assertion of NO mapping, but shouldn't be indexed (not searchable)
                if (target != null) {
                    doc.add(new Field("target",
                            target.toString(),
                            Field.Store.YES,
                            Field.Index.ANALYZED));
                }
            }

            doc.add(new Field("quality",
                    Float.toString(scoreAnnotationQuality(annotation.getProvenance())),
                    Field.Store.YES,
                    Field.Index.ANALYZED));

            // add this document to the index
            indexWriter.addDocument(doc, getAnalyzer());
        }

    }

    public void clearAnnotationIndex() {

    }

    public void createAnnotationSummaryIndex(AnnotationSummaryDAO summaryDao) throws IOException {

        getLog().info("Creating annotation summary lucene index...");

        IndexWriter summaryIndexWriter = obtainIndexWriter(getAnnotationSummaryIndex());

        final String UNTYPED = "##zooma.untyped.property.key##";

        Collection<AnnotationSummary> summaries = summaryDao.read();

        Map<String, Float> summaryIdToMaxScore = new HashMap<>();
        Map<String, Set<URI>> summaryIdToSourcesMap = new HashMap<>();

        for (AnnotationSummary summary : summaries) {

            String summaryId = null;
            // get property
            String propertyType =
                    summary.getAnnotatedPropertyType() != null ? summary.getAnnotatedPropertyType() : UNTYPED;
            String propertyValue = summary.getAnnotatedPropertyValue();
            // get semantic tags
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
                    AnnotationProvenance prov = getAnnotationProvenanceDAO().read(annoUri);
                    float annotationScore = scoreAnnotationQuality(prov);
                    if (!summaryIdToMaxScore.containsKey(summaryId) ||
                            (annotationScore > summaryIdToMaxScore.get(summaryId))) {
                        summaryIdToMaxScore.put(summaryId, scoreAnnotationQuality(prov));
                    }
                    summaryIdToSourcesMap.get(summaryId).add(prov.getSource().getURI());
                }

                // build one document to index each summary combination
                Document doc = new Document();
                doc.add(new Field("id",
                        summaryId,
                        Field.Store.YES,
                        Field.Index.ANALYZED));
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
                summaryIndexWriter.addDocument(doc, getAnalyzer());

            }

        }
        getLog().info("Annotation summary lucene indexing complete!");
        summaryIndexWriter.close();

    }

    public void createAnnotationSummaryIndex(Collection<Annotation> annotations, IndexWriter writer) throws IOException {
        getLog().debug("Creating annotation summary lucene index...");

        // key reference for any untyped properties
        final String UNTYPED = "##zooma.untyped.property.key##";

        // build the set of annotation summaries - i.e. unique combinations
        Map<String, Map<String, Set<String>>> termToValueToSummaryIdMap = new HashMap<>();
        Map<String, Set<URI>> summaryIdToSemanticTagsMap = new HashMap<>();
        Map<String, Set<Annotation>> summaryIdToAnnotationsMap = new HashMap<>();
        Map<String, Float> summaryIdToMaxScore = new HashMap<>();
        Map<String, Set<URI>> summaryIdToSourcesMap = new HashMap<>();

        getLog().debug("Evaluating unique combinations for " + annotations.size() + " annotations...");
        for (Annotation annotation : annotations) {
            // the summary ID for this combination
            String summaryId = null;

            // get property
            Property property = annotation.getAnnotatedProperty();
            // get property type
            String propertyType =
                    property instanceof TypedProperty ? ((TypedProperty) property).getPropertyType() : UNTYPED;
            // get property value
            String propertyValue = property.getPropertyValue();
            // get semantic tags
            Collection<URI> semanticTags = annotation.getSemanticTags();

            // do type index lookup, add if absent
            if (!termToValueToSummaryIdMap.containsKey(propertyType)) {
                termToValueToSummaryIdMap.put(propertyType, new HashMap<String, Set<String>>());
            }
            Map<String, Set<String>> valueToSummaryIdMap = termToValueToSummaryIdMap.get(propertyType);

            // do value index lookup, add if absent
            if (!valueToSummaryIdMap.containsKey(propertyValue)) {
                valueToSummaryIdMap.put(propertyValue, new HashSet<String>());
            }
            Set<String> summaryIdSet = valueToSummaryIdMap.get(propertyValue);

            // compare all semantic tags in this annotation to those for all summary IDs - do they match?
            boolean matchedExisting = false;
            for (String targetSummaryId : summaryIdSet) {
                if (summaryIdToAnnotationsMap.containsKey(targetSummaryId)) {
                    for (Annotation targetAnnotation : summaryIdToAnnotationsMap.get(targetSummaryId)) {
                        Collection<URI> targetSemanticTags = targetAnnotation.getSemanticTags();
                        if (CollectionUtils.compareCollectionContents(semanticTags, targetSemanticTags)) {
                            // this is the "same" combination, so grab ID and break
                            getLog().trace("Annotation " + annotation + " has a combination of property and " +
                                    "semantic tags that match " + targetAnnotation);
                            matchedExisting = true;
                            summaryId = targetSummaryId;
                            break;
                        }
                    }
                }
            }

            // is this a new combination?
            if (matchedExisting) {
                // increment verified count
                // this is an existing combination, so just track the source of the new annotation
                summaryIdToSourcesMap.get(summaryId).add(annotation.getProvenance().getSource().getURI());
                getLog().trace("Annotation summary datasource verification: " +
                        "property value '" + propertyValue + "', type '" + propertyType + "' " +
                        "maps to " + semanticTags + " (" + summaryId + ") " +
                        "now verified " + summaryIdToSourcesMap.get(summaryId).size() + " times");
            }
            else {
                // this is a novel combination, so generate a new index entry...

                // generate summary id
                List<String> idContent = new ArrayList<>();
                for (URI uri : semanticTags) {
                    if (uri != null) {
                        // append URI to ID
                        idContent.add(uri.toString());
                    }
                }

                // if there are annotations with NO semantic tags, generate no summary
                if (idContent.size() > 0) {
                    // add property type and value to id content
                    idContent.add(0, propertyValue);
                    idContent.add(0, propertyType);

                    summaryId = generateEncodedID(idContent.toArray(new String[idContent.size()]));
                    getLog().trace("Found new unique combination - " +
                            "property value '" + propertyValue + "', type '" + propertyType + "' " +
                            "maps to " + semanticTags + " (" + summaryId + ")");

                    // add this summary id into the set of known IDs
                    summaryIdSet.add(summaryId);

                    // and add this summary id as key into the annotations map
                    summaryIdToAnnotationsMap.put(summaryId, new HashSet<Annotation>());

                    // also add this summary id as key into semantic tags map, along with a set to hold those entities
                    summaryIdToSemanticTagsMap.put(summaryId, new HashSet<URI>());

                    // also add this summary id as key into the sources map
                    summaryIdToSourcesMap.put(summaryId, new HashSet<URI>());
                }
            }

            // if summaryId is null, there is no summary to generate
            if (summaryId != null) {
                // add the new annotations and described entities to the required maps and link to annotations, indexed by the summary ID
                summaryIdToAnnotationsMap.get(summaryId).add(annotation);
                summaryIdToSemanticTagsMap.get(summaryId).addAll(annotation.getSemanticTags());
                summaryIdToSourcesMap.get(summaryId).add(annotation.getProvenance().getSource().getURI());

                // check annotation score against current max - if no current max, or if greater, replace
                float annotationScore = scoreAnnotationQuality(annotation.getProvenance());
                if (!summaryIdToMaxScore.containsKey(summaryId) ||
                        (annotationScore > summaryIdToMaxScore.get(summaryId))) {
                    summaryIdToMaxScore.put(summaryId, scoreAnnotationQuality(annotation.getProvenance()));
                }

                getLog().trace("Summary ID: '" + summaryId + "'\n\t" +
                        "now links to annotation '" + annotation + "' " +
                        "(annotations now: " + summaryIdToAnnotationsMap.get(summaryId).size() +
                        ")\n\t" +
                        "and semantic tags " + annotation.getSemanticTags() + " " +
                        "(semantic tags now: " +
                        summaryIdToSemanticTagsMap.get(summaryId).size() + ")");
            }
        }


        // take map (containing unique combos) and build index
        for (String propertyType : termToValueToSummaryIdMap.keySet()) {
            getLog().trace("Next summary property type: " + propertyType);
            Map<String, Set<String>> valueToSummaryIdMap = termToValueToSummaryIdMap.get(propertyType);
            for (String propertyValue : valueToSummaryIdMap.keySet()) {
                getLog().trace("Next summary property value: " + propertyValue);
                Set<String> summaryIDs = valueToSummaryIdMap.get(propertyValue);
                for (String summaryID : summaryIDs) {
                    getLog().trace("Next annotation summary ID: " + summaryID);

                    // build one document to index each summary combination
                    Document doc = new Document();
                    doc.add(new Field("id",
                            summaryID,
                            Field.Store.YES,
                            Field.Index.ANALYZED));
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
                    Collection<URI> semanticTags = summaryIdToSemanticTagsMap.get(summaryID);
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
                    Collection<Annotation> indexedAnnotations = summaryIdToAnnotationsMap.get(summaryID);
                    for (Annotation annotation : indexedAnnotations) {
                        // add a field for this URI
                        getLog().trace("Next summary annotation: " + annotation);
                        doc.add(new Field("annotation",
                                annotation.getURI().toString(),
                                Field.Store.YES,
                                Field.Index.ANALYZED));
                    }
                    // add a field for the frequency of use of this pattern
                    getLog().trace("Summary frequency: " + indexedAnnotations.size());
                    doc.add(new Field("frequency",
                            Integer.toString(indexedAnnotations.size()),
                            Field.Store.YES,
                            Field.Index.ANALYZED));
                    getLog().trace("Best score: " + summaryIdToMaxScore.get(summaryID));
                    doc.add(new Field("topScore",
                            Float.toString(summaryIdToMaxScore.get(summaryID)),
                            Field.Store.YES,
                            Field.Index.ANALYZED));
                    getLog().trace("Number of times verified: " + summaryIdToSourcesMap.get(summaryID).size());
                    doc.add(new Field("timesVerified",
                            Integer.toString(summaryIdToSourcesMap.get(summaryID).size()),
                            Field.Store.YES,
                            Field.Index.ANALYZED));

                    getLog().trace("Annotation Summary index entry:\n\t" +
                            "ID: " + summaryID + ",\n\t" +
                            "Property: " + propertyValue + ",\n\t" +
                            "Property Type: " + propertyType + ",\n\t" +
                            "Semantic Tags: " + semanticTags.toString() + ",\n\t" +
                            "Summary Frequency: " + indexedAnnotations.size() + ",\n\t" +
                            "Best score: " + summaryIdToMaxScore.get(summaryID) + ",\n\t" +
                            "Times verified: " + summaryIdToSourcesMap.get(summaryID).size());

                    // add this document to the index
                    writer.addDocument(doc, getAnalyzer());
                }
            }
        }
    }

    public void clearAnnotationSummaryIndex() {

    }

    @Override
    protected void doInitialization() throws Exception {
        getLog().info("ZOOMA index directory: " + System.getProperty("zooma.home"));
        getLog().info("Cleaning lucene indices...");
        clearAnnotationSummaryIndex();
        clearAnnotationIndex();
        clearAnnotationCountIndex();
        clearPropertyIndices();
        getLog().info("Querying underlying datasources for properties to index...");
        Collection<Property> properties =
                getMaxEntityCount() == -1 ? getPropertyDAO().read() : getPropertyDAO().read(getMaxEntityCount(), 0);
//        getLog().info("Querying underlying datasources for annotations to index...");
//        Collection<Annotation> annotations =
//                getMaxEntityCount() == -1 ? getAnnotationDAO().read() : getAnnotationDAO().read(getMaxEntityCount(), 0);
        getLog().info("Building lucene indices...");
        createPropertyIndices(properties);
        getLog().info("Querying underlying datasources for annotations to index...");

        Collection<Annotation> annotations = getAnnotationDAO().read();

        int count = getMaxEntityCount() == -1 ? annotations.size(): getMaxEntityCount();
        getLog().info("Total annotation to index:" + count);
        createAnnotationCountIndex(count);
        createAnnotationIndex(new ArrayList<Annotation>(annotations));
        createAnnotationSummaryIndex(getAnnotationSummaryDAO());
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
        IndexWriterConfig config = new IndexWriterConfig(VERSION, getAnalyzer());
        // TODO - always recreates index from scratch, incremental strategy needed?
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
        // followed by source - ranked list?
        int rank = getSourceRanking(prov.getSource().getURI());
        // creation time should then work backwards from most recent to oldest
        long age = prov.getGeneratedDate().getTime();

        float score = (float) ((evidenceScore * Math.sqrt(rank)) + Math.log(age));
        getLog().trace("Evaluated score of annotation '" + prov + "' as " + score);
        return score;
    }

    protected int getSourceRanking(URI source) {
        // TODO - configurable source rankings, not hard coded?
        if (source.toString().equals("http://www.ebi.ac.uk/fgpt/zooma")) {
            return 3;
        }
        else if (source.toString().equals("http://www.ebi.ac.uk/gxa")) {
            return 2;
        }
        else if (source.toString().equals("http://www.ebi.ac.uk/gwas")) {
            return 2;
        }
        else {
            return 1;
        }
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
