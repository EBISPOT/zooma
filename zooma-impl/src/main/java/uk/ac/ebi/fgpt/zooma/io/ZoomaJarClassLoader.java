package uk.ac.ebi.fgpt.zooma.io;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A class loader that loads *.jar files, either individually by jar or by locating all jar file within in a directory.
 *
 * @author Tony Burdett
 * @date 27/06/14
 */
public class ZoomaJarClassLoader extends URLClassLoader {
    private final String loaderName;

    public ZoomaJarClassLoader(URL jarUrl, ClassLoader parent, String loaderName) {
        super(new URL[]{jarUrl}, parent);
        this.loaderName = loaderName;
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "_" + loaderName;
    }
}
