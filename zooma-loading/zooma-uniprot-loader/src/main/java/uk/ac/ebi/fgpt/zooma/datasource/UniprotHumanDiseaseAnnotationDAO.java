package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.core.io.Resource;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * An annotation DAO that is capable of retrieving annotations from Uniprot's Human Disease keyword catalog (see <a
 * href="http://www.uniprot.org/docs/humdisease.txt">http://www.uniprot.org/docs/humdisease.txt</a>).  It is possible to
 * configure this DAO to cache the results obtained from Uniprot in memory for a given amount of time.
 *
 * @author Tony Burdett
 * @date 04/07/14
 */
public class UniprotHumanDiseaseAnnotationDAO extends Initializable implements AnnotationDAO {
    public static final int READ_AHEAD = 1024 * 1024;

    private final AnnotationFactory annotationFactory;

    private Resource uniprotHumdiseaseResource;

    private boolean isCachingEnabled = true;
    private int invalidateAfter = 24;

    private UniprotHumanDiseaseVocabReader reader;
    private boolean resourceSupportsMark;

    private List<Annotation> cachedAnnotations;
    private long cacheTimestamp = 0;

    public UniprotHumanDiseaseAnnotationDAO() {
        this(new UniprotHumanDiseaseAnnotationFactory(new UniprotHumanDiseaseAnnotationSession()));
    }

    public UniprotHumanDiseaseAnnotationDAO(AnnotationFactory annotationFactory) {
        this.annotationFactory = annotationFactory;
    }

    public Resource getUniprotHumanDiseaseResource() {
        return uniprotHumdiseaseResource;
    }

    /**
     * Sets the location of the Uniprot Human Disease resource that data will be obtained from.
     *
     * @param uniprotHumdiseaseResource the location to acquire the Uniprot Human disease keywords from
     */
    public void setUniprotHumanDiseaseResource(Resource uniprotHumdiseaseResource) {
        this.uniprotHumdiseaseResource = uniprotHumdiseaseResource;
    }

    public boolean isCachingEnabled() {
        return isCachingEnabled;
    }

    /**
     * Sets whether to cache annotations after reading.  If enabled, annotations will be cached instead of re-streamed
     * on each method call. This is true by default to minimize network traffic.  The cache is invalidated after an
     * amount of time specified by {@link #getInvalidateAfter()} expires.
     *
     * @return whether to cache
     */
    public void setCachingEnabled(boolean isCachingEnabled) {
        this.isCachingEnabled = isCachingEnabled;
    }

    public int getInvalidateAfter() {
        return invalidateAfter;
    }

    /**
     * Sets the amount of time, in hours, after which cached annotations should be invalidated.  This is set to 24 hours
     * by default.
     *
     * @param invalidateAfter the time after which the annotation cache should be invalidated
     */
    public void setInvalidateAfter(int invalidateAfter) {
        this.invalidateAfter = invalidateAfter;
    }

    @Override public Collection<Annotation> readByStudy(Study study) {
        throw new UnsupportedOperationException("Cannot query Uniprot keywords vocabulary by study");
    }

    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        throw new UnsupportedOperationException("Cannot query Uniprot keywords vocabulary by biological entity");
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        throw new UnsupportedOperationException("Property lookup has not yet been implemented");
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        throw new UnsupportedOperationException("Semantic tag lookup has not yet been implemented");
    }

    @Override public String getDatasourceName() {
        return "uniprot-humdisease";
    }

    @Override public int count() {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }

        try {
            return loadAnnotations().size();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read annotations from uniprot resource", e);
        }
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
    }

    @Override
    public void create(Collection<Annotation> annotations) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
    }

    @Override public Collection<Annotation> read() {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }

        try {
            return loadAnnotations();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read annotations from uniprot resource", e);
        }
    }

    @Override public List<Annotation> read(int size, int start) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }

        try {
            List<Annotation> allAnnotations = loadAnnotations();
            int end = start + size > allAnnotations.size() ? allAnnotations.size() : start + size;
            return allAnnotations.subList(start, end);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read annotations from uniprot resource", e);
        }
    }

    @Override public Annotation read(URI uri) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support URI based lookups");
    }

    @Override public void update(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, updates not supported");
    }

    @Override public void update(Collection<Annotation> object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, updates not supported");
    }

    @Override public void delete(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, deletions not supported");
    }


    @Override protected void doInitialization() throws Exception {
        // create a Uniprot entry reader from the uniprot human disease resource
        this.resourceSupportsMark = uniprotHumdiseaseResource.getInputStream().markSupported();
        resetReader();
    }

    @Override protected void doTermination() throws Exception {
        reader.close();
    }

    private void resetReader() throws IOException {
        boolean recreate = false;
        if (resourceSupportsMark) {
            if (reader == null) {
                reader = createReader();
                try {
                    reader.mark(READ_AHEAD);
                }
                catch (IOException e) {
                    getLog().error(
                            "Failed to mark Uniprot humdisease stream with a read-ahead of " + READ_AHEAD + "; " +
                                    "marking will be disabled (" + e.getMessage() + ")");
                    resourceSupportsMark = false;
                }
            }
            else {
                try {
                    reader.reset();
                }
                catch (IOException e) {
                    getLog().error("Failed to reset Uniprot humdisease stream; reader will be recreated", e);
                    recreate = true;
                }
            }
        }
        else {
            recreate = true;
        }

        if (recreate) {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (IOException e) {
                // ignore
                getLog().warn("Closing existing UniprotHumanDiseaseEntryReader failed; this may cause a memory leak",
                              e);
            }
            reader = createReader();
        }
    }

    private UniprotHumanDiseaseVocabReader createReader() throws IOException {
        InputStream in = uniprotHumdiseaseResource.getInputStream();
        return new UniprotHumanDiseaseVocabReader(in);
    }

    private List<Annotation> loadAnnotations() throws IOException {
        long timecheck = System.currentTimeMillis() - cacheTimestamp;
        boolean invalid = timecheck > (getInvalidateAfter() * 1000 * 60 * 60);
        if (isCachingEnabled() && cachedAnnotations != null && !invalid) {
            return cachedAnnotations;
        }
        else {
            List<UniprotHumanDiseaseEntry> entries = readEntries();
            List<Annotation> results = new ArrayList<>();
            for (UniprotHumanDiseaseEntry entry : entries) {
                results.addAll(convertEntry(entry));
            }
            if (isCachingEnabled()) {
                cachedAnnotations = results;
                cacheTimestamp = System.currentTimeMillis();
            }
            return results;
        }
    }

    private List<UniprotHumanDiseaseEntry> readEntries() throws IOException {
        resetReader();
        List<UniprotHumanDiseaseEntry> entries = new ArrayList<>();
        UniprotHumanDiseaseEntry entry;
        int i = 0;
        while ((entry = reader.readEntry()) != null) {
            entries.add(entry);
            i++;
            if (getLog().isTraceEnabled()) {
                getLog().trace("Read next uniprot entry, '" + entry.getAccession() + "', now done " + i);
            }
        }
        return entries;
    }

    private Collection<Annotation> convertEntry(UniprotHumanDiseaseEntry entry) {
        if (getLog().isTraceEnabled()) {
            getLog().trace("Converting Uniprot entry '" + entry.getAccession() + "' to ZOOMA annotations...");
        }
        Collection<Annotation> results = new HashSet<>();
        results.add(annotationFactory.createAnnotation(null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       "disease state",
                                                       entry.getName(),
                                                       null,
                                                       null,
                                                       convertToSemanticTag(entry.getOmimID()),
                                                       null,
                                                       null));
        for (String altTitle : entry.getSynonyms()) {
            results.add(annotationFactory.createAnnotation(null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           "disease state",
                                                           altTitle,
                                                           null,
                                                           null,
                                                           convertToSemanticTag(entry.getOmimID()),
                                                           null,
                                                           null));
        }
        if (getLog().isTraceEnabled()) {
            getLog().trace(
                    "Uniprot entry '" + entry.getAccession() + "' resulted in " + results.size() + " annotations");
        }
        return results;
    }

    private URI convertToSemanticTag(String omimID) {
        return URI.create("http://omim.org/entry/" + omimID);
    }
}
