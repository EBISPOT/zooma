package uk.ac.ebi.fgpt.zooma.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaLoadingException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaResolutionException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.Update;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link uk.ac.ebi.fgpt.zooma.io.ZoomaLoader} for {@link Annotation}s that can be configured to enable resolving,
 * serializing and storage of annotations.
 *
 * @author Tony Burdett
 * @date 11/06/13
 */
@Deprecated
public class ConfigurableAnnotationLoader implements ZoomaLoader<Annotation> {
    private final String outputPath;
    private final String fileNameBase;

    private Map<String, AtomicInteger> datasourceFileCounter;

    private ZoomaResolver<Annotation> annotationResolver;
    private ZoomaSerializer<Annotation, ?, ?> annotationSerializer;
    private ZoomaStorer annotationStorer;

    private boolean resolvingEnabled;
    private boolean serializingEnabled;
    private boolean storingEnabled;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    /**
     * Create a new loader instance, given the path to the output directory where data files should be generated, and a
     * base filename to use.  This loader implementation stores datafiles in subdirectories named after the datasource,
     * and names files using the base name supplied and an incremented number.  So, for example, if you supply an
     * outputPath of "/tmp/my-zooma-files/" and a fileNameBase of "zooma-data", then load data from the datasource "foo"
     * you can expect to find files like: &nbsp;&nbsp;&nbsp;/tmp/my-zooma-files<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/foo<br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/zooma-data_1.rdf<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/zooma-data_2.rdf<br> ...and so on
     *
     * @param outputPath   the path to the output directory where you want to store data files
     * @param fileNameBase the base name to use for namign new data files
     */
    public ConfigurableAnnotationLoader(String outputPath, String fileNameBase) {
        this.outputPath = outputPath;
        this.fileNameBase = fileNameBase;
        this.datasourceFileCounter = new HashMap<>();
    }

    public ZoomaResolver<Annotation> getAnnotationResolver() {
        return annotationResolver;
    }

    public void setAnnotationResolver(ZoomaResolver<Annotation> annotationResolver) {
        this.annotationResolver = annotationResolver;
    }

    public ZoomaSerializer<Annotation, ?, ?> getAnnotationSerializer() {
        return annotationSerializer;
    }

    public void setAnnotationSerializer(ZoomaSerializer<Annotation, ?, ?> annotationSerializer) {
        this.annotationSerializer = annotationSerializer;
    }

    public ZoomaStorer getAnnotationStorer() {
        return annotationStorer;
    }

    public void setAnnotationStorer(ZoomaStorer annotationStorer) {
        this.annotationStorer = annotationStorer;
    }

    public boolean isResolvingEnabled() {
        return resolvingEnabled;
    }

    public void setResolvingEnabled(boolean resolvingEnabled) {
        this.resolvingEnabled = resolvingEnabled;
    }

    public boolean isSerializingEnabled() {
        return serializingEnabled;
    }

    public void setSerializingEnabled(boolean serializingEnabled) {
        this.serializingEnabled = serializingEnabled;
    }

    public boolean isStoringEnabled() {
        return storingEnabled;
    }

    public void setStoringEnabled(boolean storingEnabled) {
        this.storingEnabled = storingEnabled;
    }

    @Override
    public void load(String datasourceName, Collection<Annotation> annotations) throws ZoomaLoadingException {
        File outputDirectory = null;
        try {
            outputDirectory = createDatasourceDirs(datasourceName);

            // resolve
            if (isResolvingEnabled()) {
                getLog().info("Resolving " + annotations.size() + " annotations for " + datasourceName);
                annotations = getAnnotationResolver().resolve(datasourceName, annotations);
            }

            // serialize
            if (isSerializingEnabled()) {
                getLog().info(
                        "Serializing " + annotations.size() + " annotations for " + datasourceName);

                if (!datasourceFileCounter.containsKey(datasourceName)) {
                    datasourceFileCounter.put(datasourceName, new AtomicInteger(0));
                }
                AtomicInteger fileCounter = datasourceFileCounter.get(datasourceName);
                int fileNumber = fileCounter.incrementAndGet();

                String filename = fileNameBase + "_" + fileNumber + ".rdf";
                File f = new File(outputDirectory, filename);
                while (!f.createNewFile()) {
                    fileNumber = fileCounter.incrementAndGet();
                    filename = fileNameBase + "_" + fileNumber + ".rdf";
                    f = new File(outputDirectory, filename);
                }
                getAnnotationSerializer().serialize(datasourceName, annotations, f);

                // store (can only store if also serialized!)
                if (isStoringEnabled()) {
                    getLog().info("Storing " + annotations.size() + " annotations for " + datasourceName + " from "
                                          + f.getAbsolutePath());
                    getAnnotationStorer().store(f);
                }
            }
        }
        catch (ZoomaSerializationException e) {
            if (outputDirectory != null) {
                throw new ZoomaLoadingException(
                        "Unable to load annotations due to problems writing files out to output directory " +
                                outputDirectory.getAbsolutePath(),
                        e);
            }
            else {
                throw new ZoomaLoadingException(
                        "Unable to load annotations due to problems writing files out to output directory " +
                                outputPath + File.separator + datasourceName,
                        e);
            }
        }
        catch (IOException e) {
            throw new ZoomaLoadingException(
                    "Unable to load annotations due to problems storing files in repository",
                    e);
        }
        catch (ZoomaResolutionException e) {
            throw new ZoomaLoadingException(
                    "Unable to load annotations due to problems resolving annotations against ZOOMA",
                    e);
        }
    }

    @Override
    public void load(Annotation annotation) throws ZoomaLoadingException {
        load("single-annotation", Collections.singleton(annotation));
    }

    @Override
    public void loadSupplementaryData(String datasourceName, InputStream rdfInputStream)
            throws ZoomaLoadingException {
        File outputDirectory = null;
        try {
            outputDirectory = createDatasourceDirs(datasourceName);

            // serialize
            if (isSerializingEnabled()) {
                getLog().debug("Serializing supplementary data for " + datasourceName);
                String filename = datasourceName + "_supplemental.rdf";
                File f = new File(outputDirectory, filename);
                FileOutputStream rdfOutputStream = new FileOutputStream(f);

                // read bytes from input stream, write to file
                try {
                    byte[] buffer = new byte[1024];
                    int len = rdfInputStream.read(buffer);
                    while (len != -1) {
                        rdfOutputStream.write(buffer, 0, len);
                        len = rdfInputStream.read(buffer);
                    }
                }
                finally {
                    rdfInputStream.close();
                    rdfOutputStream.close();
                }
            }
        }
        catch (IOException e) {
            throw new ZoomaLoadingException(
                    "Unable to load annotations due to problems storing files in repository",
                    e);
        }
        catch (ZoomaSerializationException e) {
            if (outputDirectory != null) {
                throw new ZoomaLoadingException(
                        "Unable to load annotations due to problems writing files out to output directory " +
                                outputDirectory.getAbsolutePath(),
                        e);
            }
            else {
                throw new ZoomaLoadingException(
                        "Unable to load annotations due to problems writing files out to output directory " +
                                outputPath + File.separator + datasourceName,
                        e);
            }
        }
    }

    @Override
    public void update(Collection<Annotation> zoomaObject, Update<Annotation> update) throws ZoomaLoadingException {
        throw new UnsupportedOperationException("Can't update annotations from configurable loader");
    }

    private final static ReentrantLock lock = new ReentrantLock();

    /**
     * A thread-safe implementation of {@link java.io.File#mkdirs()} that creates all required parent directories for
     * the given file if they do not already exist.  This method will return early if the directories already exist.  If
     * parent directories are absent, this method will acquire a reentrant lock, thereby ensuring that only one thread
     * will attempt directory creation.  If directory creation still fails, this method will throw an IO exception
     *
     * @param f the file to create parent directories for, if they do not already exist
     */
    private void createDirs(File f) throws ZoomaSerializationException {
        if (f.isDirectory()) {
            if (!f.getAbsoluteFile().exists()) {
                lock.lock();
                try {
                    // retest; another thread may have created this directory between the first test and acquiring the lock
                    if (!f.getAbsoluteFile().exists()) {
                        if (!f.getAbsoluteFile().mkdirs()) {
                            throw new ZoomaSerializationException(
                                    "Unable to create directory '" + f.getAbsolutePath() + "'");
                        }
                    }
                }
                finally {
                    lock.unlock();
                }
            }
        }
        else {
            if (!f.getAbsoluteFile().getParentFile().exists()) {
                lock.lock();
                try {
                    // retest; another thread may have created this directory between the first test and acquiring the lock
                    if (!f.getAbsoluteFile().getParentFile().exists()) {
                        if (!f.getAbsoluteFile().getParentFile().mkdirs()) {
                            throw new ZoomaSerializationException(
                                    "Unable to create directory '" + f.getParentFile().getAbsolutePath() + "'");
                        }
                    }
                }
                finally {
                    lock.unlock();
                }
            }
        }
    }

    private File createDatasourceDirs(String datasourceName) throws ZoomaSerializationException {
        // make the output directory if it doesn't exist
        File outputDirectory = new File(outputPath);
        if (!outputDirectory.exists()) {
            getLog().info("Creating output directory '" + outputDirectory.getAbsolutePath() + "'...");
            boolean created = outputDirectory.mkdirs();
            if (created) {
                getLog().info("'" + outputDirectory.getAbsolutePath() + "' created ok");
            }
        }

        // calculate the datasource path
        StringBuilder datasourcePath = new StringBuilder();
        String[] tokens = datasourceName.trim().split("\\.");
        for (String token : tokens) {
            datasourcePath.append(token).append(File.separator);
        }

        File datasourceDir = new File(outputDirectory, datasourcePath.toString());
        createDirs(datasourceDir);

        // create the named graph file in the datasource directory
        String filename = "global.graph";
        File f = new File(outputDirectory, filename);
        // read bytes from input stream, write to file
        try (PrintWriter writer =
                     new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f))))) {
            writer.println(URI.create(URI.create(Namespaces.ZOOMA.getURI() + datasourceName).toString()));
        }
        catch (IOException e) {
            getLog().warn("Could not create named graph file '" + f.getAbsolutePath() + "' " +
                                  "for datasource '" + datasourceName + "' - " +
                                  "annotations will be stored in the default named graph");
        }

        return datasourceDir;
    }
}
