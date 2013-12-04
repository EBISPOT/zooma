package uk.ac.ebi.fgpt.zooma.datasource;

import org.biojava3.core.util.UncompressInputStream;
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
 * An annotation DAO that is capable of retrieving annotations from OMIM over FTP.  It is possible to configure this DAO
 * to cache the results obtained from OMIM in memory for a given amount of time.
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimAnnotationDAO extends Initializable implements AnnotationDAO {
    public static final int READ_AHEAD = 1024 * 1024;

    private final AnnotationFactory annotationFactory;

    private Resource omimResource;

    private boolean isCompressed = false;
    private boolean isCachingEnabled = true;
    private int invalidateAfter = 24;

    private OmimPhenotypeEntryReader reader;
    private boolean resourceSupportsMark;

    private List<Annotation> cachedAnnotations;
    private long cacheTimestamp = 0;

    public OmimAnnotationDAO() {
        this(new OmimAnnotationFactory(new OmimLoadingSession()));
    }

    public OmimAnnotationDAO(AnnotationFactory annotationFactory) {
        this.annotationFactory = annotationFactory;
    }

    public Resource getOmimResource() {
        return omimResource;
    }

    /**
     * Sets the location of OMIM that data will be obtained from.  FTP urls are acceptable here, but bear in mind OMIM
     * access requires user credentials.
     *
     * @param omimResource the location to acquire OMIM from
     */
    public void setOmimResource(Resource omimResource) {
        this.omimResource = omimResource;
    }

    /**
     * Sets whether to read OMIM using a compressed stream.  To decompress OMIM, this implementation uses an {@link
     * org.biojava3.core.util.UncompressInputStream}, as OMIM is served in a form using the compress unix tool.
     *
     * @return a flag indicating whether the compressed version of OMIM is being accessed
     */
    public boolean isCompressed() {
        return isCompressed;
    }

    /**
     * Sets whether to cache annotations after reading.  If enabled, annotations will be cached instead of re-streamed
     * on each method call. This is true by default to minimize network traffic.  The cache is invalidated after an
     * amount of time specified by {@link #getInvalidateAfter()} expires.
     *
     * @return whether to cache
     */
    public void setCompressed(boolean compressed) {
        isCompressed = compressed;
    }

    public boolean isCachingEnabled() {
        return isCachingEnabled;
    }

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
        throw new UnsupportedOperationException("Cannot query OMIM by study");
    }

    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        throw new UnsupportedOperationException("Cannot query OMIM by biological entity");
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        throw new UnsupportedOperationException("Property lookup not yet implemented");
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        throw new UnsupportedOperationException("Semantic tag lookup not yet implemented");
    }

    @Override public String getDatasourceName() {
        return "omim";
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
            throw new RuntimeException("Failed to read annotations from OMIM resource", e);
        }
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
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
            throw new RuntimeException("Failed to read annotations from OMIM resource", e);
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
            throw new RuntimeException("Failed to read annotations from OMIM resource", e);
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

    @Override public void delete(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, deletions not supported");
    }

    @Override protected void doInitialization() throws Exception {
        // create an OMIM entry reader from the omim resource
        this.resourceSupportsMark = omimResource.getInputStream().markSupported();
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
                    getLog().error("Failed to mark OMIM stream with a read-ahead of " + READ_AHEAD + "; " +
                                           "marking will be disabled (" + e.getMessage() + ")");
                    resourceSupportsMark = false;
                }
            }
            else {
                try {
                    reader.reset();
                }
                catch (IOException e) {
                    getLog().error("Failed to reset OMIM stream; reader will be recreated", e);
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
                getLog().warn("Closing existing OMIMEntryReader failed; this may cause a memory leak", e);
            }
            reader = createReader();
        }
    }

    private OmimPhenotypeEntryReader createReader() throws IOException {
        InputStream in;
        if (isCompressed()) {
            in = new UncompressInputStream(omimResource.getInputStream());
        }
        else {
            in = omimResource.getInputStream();
        }
        return new OmimPhenotypeEntryReader(in);
    }

    private List<Annotation> loadAnnotations() throws IOException {
        long timecheck = System.currentTimeMillis() - cacheTimestamp;
        boolean invalid = timecheck > (getInvalidateAfter() * 1000 * 60 * 60);
        if (isCachingEnabled() && cachedAnnotations != null && !invalid) {
            return cachedAnnotations;
        }
        else {
            List<OmimPhenotypeEntry> entries = readEntries();
            List<Annotation> results = new ArrayList<>();
            for (OmimPhenotypeEntry entry : entries) {
                results.addAll(convertEntry(entry));
            }
            if (isCachingEnabled()) {
                cachedAnnotations = results;
                cacheTimestamp = System.currentTimeMillis();
            }
            return results;
        }
    }

    private List<OmimPhenotypeEntry> readEntries() throws IOException {
        resetReader();
        List<OmimPhenotypeEntry> entries = new ArrayList<>();
        OmimPhenotypeEntry entry;
        int i = 0;
        while ((entry = reader.readEntry()) != null) {
            entries.add(entry);
            i++;
            if (getLog().isTraceEnabled()) {
                getLog().trace("Read next OMIM entry, '" + entry.getOmimID() + "', now done " + i);
            }
        }
        return entries;
    }

    private Collection<Annotation> convertEntry(OmimPhenotypeEntry entry) {
        if (getLog().isTraceEnabled()) {
            getLog().trace("Converting OMIM entry '" + entry.getOmimID() + "' to ZOOMA annotations...");
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
                                                       "OMIM phenotype",
                                                       entry.getPreferredTitle(),
                                                       null,
                                                       null,
                                                       convertToSemanticTag(entry.getOmimID()),
                                                       entry.getLastAnnotator(),
                                                       entry.getLastAnnotationDate()));
        for (String altTitle : entry.getAlternativeTitles()) {
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
                                                           "OMIM phenotype",
                                                           altTitle,
                                                           null,
                                                           null,
                                                           convertToSemanticTag(entry.getOmimID()),
                                                           entry.getLastAnnotator(),
                                                           entry.getLastAnnotationDate()));
        }
        if (getLog().isTraceEnabled()) {
            getLog().trace("OMIM entry '" + entry.getOmimID() + "' resulted in " + results.size() + " annotations");
        }
        return results;
    }

    private URI convertToSemanticTag(String omimID) {
        return URI.create("http://omim.org/entry/" + omimID);
    }
}
