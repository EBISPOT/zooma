package uk.ac.ebi.fgpt.zooma.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;

/**
 * A class loader that loads *.jar files, either individually by jar or by locating all jar file within in a directory.
 *
 * @author Tony Burdett
 * @date 27/06/14
 */
public class JarFileClassLoader extends URLClassLoader {
    private static final Logger log = LoggerFactory.getLogger(JarFileClassLoader.class);

    public JarFileClassLoader(File f) {
        super(locateURLs(f));
    }

    public JarFileClassLoader(File f, ClassLoader parent) {
        super(locateURLs(f), parent);
    }

    private static URL[] locateURLs(File f) {
        try {
            log.debug("Discovering loader modules...");
            Collection<URL> urls = new HashSet<>();
            if (f.isDirectory()) {
                FilenameFilter filter = new FilenameFilter() {
                    @Override public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                };
                for (File next : f.listFiles(filter)) {
                    log.debug("Adding loader module '" + next.getAbsolutePath() + "' to dynamic class loader");
                    urls.add(next.getAbsoluteFile().toURI().toURL());
                }
            }
            else {
                log.debug("Adding loader module '" + f.getAbsolutePath() + "' to dynamic class loader");
                urls.add(f.getAbsoluteFile().toURI().toURL());
            }
            return urls.toArray(new URL[urls.size()]);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException("Failed to load jar file(s) from " + f.getAbsolutePath(), e);
        }
    }
}
