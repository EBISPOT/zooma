package uk.ac.ebi.fgpt.zooma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.io.ZoomaCoreClassLoader;
import uk.ac.ebi.fgpt.zooma.io.ZoomaJarClassLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A spring {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} that dynamically loads additional
 * ZOOMA loader implementations.  A "loader" is a jar file containing (minimally) an {@link AnnotationDAO} and an
 * application context file called "zooma-annotation-dao.xml". Loaders are assumed to exist in the ${zooma.home}/loaders
 * directory.  This post processor loads all jars on this path directory, imports the application contexts and registers
 * them with the current bean factory.
 *
 * @author Tony Burdett
 * @date 27/06/14
 */
public class LoaderDiscoveringPostProcessor implements BeanFactoryPostProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AtomicInteger number = new AtomicInteger(1);

    protected Logger getLog() {
        return log;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // implement a class loader to load all jars in zooma.home
        File f = new File(System.getProperty("zooma.home"), "loaders");
        getLog().debug("Attempting to dynamically load loader modules from " + f.getAbsolutePath());

        List<File> loaders = new ArrayList<>();
        List<URL> dependencies = new ArrayList<>();

        int totalLoaders = 0;
        try {
            if (f.isDirectory()) {
                // if f is a directory, recurse into that directory...
                FilenameFilter filter = new FilenameFilter() {
                    @Override public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                };
                // ...and add all jars in the given directory
                for (File next : f.listFiles(filter)) {
                    URLClassLoader urlLoader = new URLClassLoader(new URL[]{next.toURI().toURL()});
                    int count = 0;
                    Enumeration<URL> urlEnum = urlLoader.findResources("zooma-annotation-dao.xml");
                    while (urlEnum.hasMoreElements()) {
                        urlEnum.nextElement();
                        count++;
                    }

                    if (count > 0) {
                        getLog().debug("ZOOMA loader module detected at '" + next.getAbsolutePath() + "': " +
                                               "a dedicated class loader will be created for this resource");
                        loaders.add(next);
                    }
                    else {
                        getLog().debug("No configuration detected in '" + next.getAbsolutePath() + "': " +
                                               "this will be loaded as a general dependency");
                        dependencies.add(next.toURI().toURL());
                    }
                }
            }
            else {
                throw new BeanDefinitionValidationException(
                        "Couldn't find loader directory at '" + f.getAbsolutePath() + "' (not a directory)");
            }

            getLog().debug("There are " + dependencies.size() + " loader modules that will be loaded " +
                                   "using the ZOOMA core class loader");
            ClassLoader zoomaCoreClassLoader =
                    new ZoomaCoreClassLoader(dependencies.toArray(new URL[dependencies.size()]),
                                             beanFactory.getBeanClassLoader());
            for (File loader : loaders) {
                totalLoaders += registerModule(zoomaCoreClassLoader, loader, beanFactory);
            }
            totalLoaders += registerConfig(zoomaCoreClassLoader, f, beanFactory);

            if (totalLoaders < 1) {
                getLog().warn("Failed to identify any loadable resources in '" + f.getAbsolutePath() + "': " +
                                      "you should check your loaders are properly configured");
            }
            else {
                getLog().info("ZOOMA has identified " + totalLoaders + " loadable resources in " + f.getAbsolutePath());
            }
        }
        catch (IOException e) {
            throw new BeanCreationException("Failed to load jar file(s) from " + f.getAbsolutePath(), e);
        }
    }

    protected int registerModule(ClassLoader zoomaCoreLoader, File f, ConfigurableListableBeanFactory beanFactory)
            throws IOException {
        getLog().debug("Registering loader module '" + f.getAbsolutePath() + "'...");
        Pattern p = Pattern.compile("^zooma-(.+)-loader-(.+)\\.jar$");
        Matcher m = p.matcher(f.getName());
        ZoomaJarClassLoader loader;
        if (m.matches()) {
            loader = new ZoomaJarClassLoader(f.getAbsoluteFile().toURI().toURL(),
                                             zoomaCoreLoader,
                                             m.group(1));
        }
        else {
            loader = new ZoomaJarClassLoader(f.getAbsoluteFile().toURI().toURL(),
                                             zoomaCoreLoader,
                                             Integer.toString(number.getAndIncrement()));
        }

        try {
            return readLoaderBeans(loader, beanFactory);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            getLog().error("Failed to register loader module at '" + f.getAbsolutePath() + "', " +
                                   "loading resources from this loader will not be available");
            getLog().debug("Failed to registter loader module at '" + f.getAbsolutePath() + "':", e);
            return 0;
        }
    }

    protected int registerConfig(ClassLoader zoomaCoreLoader, File f, ConfigurableListableBeanFactory beanFactory)
            throws IOException {
        getLog().debug("Registering additional loaders specified in config files in directory '" +
                               f.getAbsolutePath() + "'...");
        if (f.isDirectory()) {
            URLClassLoader loader = new URLClassLoader(new URL[]{f.toURI().toURL()}, zoomaCoreLoader);
            try {
                return readLoaderBeans(loader, beanFactory);
            }
            catch (IOException e) {
                throw e;
            }
            catch (Exception e) {
                getLog().error("Failed to register loader module at '" + f.getAbsolutePath() + "', " +
                                       "loading resources from this loader will not be available");
                getLog().debug("Failed to registter loader module at '" + f.getAbsolutePath() + "':", e);
                return 0;
            }
        }
        else {
            throw new BeanDefinitionValidationException(
                    "Couldn't find loader directory at '" + f.getAbsolutePath() + "' (not a directory)");
        }
    }

    protected int readLoaderBeans(URLClassLoader loader,
                                  ConfigurableListableBeanFactory beanFactory)
            throws IOException {
        int count = 0;
        Enumeration<URL> urlEnum = loader.findResources("zooma-annotation-dao.xml");
        while (urlEnum.hasMoreElements()) {
            urlEnum.nextElement();
            count++;
        }

        XmlBeanDefinitionReader classPathBeansReader =
                new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory);
        classPathBeansReader.setBeanClassLoader(loader);
        getLog().debug("Attempting to load bean definitions using " + loader.toString() + "...");
        ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver(loader);
        classPathBeansReader.setResourceLoader(resourceLoader);
        classPathBeansReader.loadBeanDefinitions("classpath*:zooma-annotation-dao.xml");
        getLog().debug("Bean definitions loaded ok!");
        return count;
    }
}
