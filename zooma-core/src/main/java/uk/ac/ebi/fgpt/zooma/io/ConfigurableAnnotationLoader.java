package uk.ac.ebi.fgpt.zooma.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaLoadingException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaResolutionException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link uk.ac.ebi.fgpt.zooma.io.ZoomaLoader} for {@link Annotation}s that can be configured to enable resolving,
 * serializing and storage of annotations.
 *
 * @author Tony Burdett
 * @date 11/06/13
 */
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
        // make the output directory if it doesn't exist
        File outputDirectory = new File(outputPath);
        if (!outputDirectory.exists()) {
            getLog().info("Creating output directory '" + outputDirectory.getAbsolutePath() + "'...");
            boolean created = outputDirectory.mkdirs();
            if (created) {
                getLog().info("'" + outputDirectory.getAbsolutePath() + "' created ok");
            }
        }

        // filter any annotations without semantic tags - ZOOMA does not do anything with these
        annotations = getAnnotationResolver().filter(annotations);

        try {
            // resolve
            if (isResolvingEnabled()) {
                getLog().debug("Resolving " + annotations.size() + " annotations for " + datasourceName);
                annotations = getAnnotationResolver().resolve(datasourceName, annotations);
            }

            // serialize
            if (isSerializingEnabled()) {
                getLog().debug(
                        "Serializing " + annotations.size() + " annotations for " + datasourceName);

                if (!datasourceFileCounter.containsKey(datasourceName)) {
                    datasourceFileCounter.put(datasourceName, new AtomicInteger(0));
                }
                AtomicInteger fileCounter = datasourceFileCounter.get(datasourceName);
                int fileNumber = fileCounter.incrementAndGet();

                StringBuilder datasourcePath = new StringBuilder();
                String[] tokens = datasourceName.trim().split("\\.");
                for (String token : tokens) {
                    datasourcePath.append(token).append(File.separator);
                }
                String filename = datasourcePath + fileNameBase + "_" + fileNumber + ".rdf";
                File f = new File(outputDirectory, filename);
                getAnnotationSerializer().serialize(datasourceName, annotations, f);

                // store (can only store if also serialized!)
                if (isStoringEnabled()) {
                    getLog().debug("Storing " + annotations.size() + " annotations for " + datasourceName + " from "
                                           + f.getAbsolutePath());
                    getAnnotationStorer().store(f);
                }
            }
        }
        catch (ZoomaSerializationException e) {
            throw new ZoomaLoadingException(
                    "Unable to load annotations due to problems writing files out to output directory " +
                            outputDirectory.getAbsolutePath(),
                    e);
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
}
