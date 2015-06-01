package uk.ac.ebi.fgpt.zooma.io;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 01/06/15
 */
public class ZoomaCoreClassLoader extends URLClassLoader {
    public ZoomaCoreClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public ZoomaCoreClassLoader(URL[] urls) {
        super(urls);
    }

    public ZoomaCoreClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override public String toString() {
        return "ZoomaCoreClassLoader";
    }
}
