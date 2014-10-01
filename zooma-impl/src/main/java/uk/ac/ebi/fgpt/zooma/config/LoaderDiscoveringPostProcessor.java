package uk.ac.ebi.fgpt.zooma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.io.ZOOMAClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

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

    protected Logger getLog() {
        return log;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // implement a class loader to load all jars in zooma.home
        File f = new File(System.getProperty("zooma.home"), "loaders");
        getLog().debug("Attempting to dynamically load loader modules from " + f.getAbsolutePath());
        ClassLoader zoomaLoader = new ZOOMAClassLoader(f, beanFactory.getBeanClassLoader());
        try {
            Enumeration<URL> urlEnum = ((ZOOMAClassLoader) zoomaLoader).findResources("zooma-annotation-dao.xml");
            int count = 0;
            while (urlEnum.hasMoreElements()) {
                urlEnum.nextElement();
                count++;
            }
            getLog().debug("There are " + count + " available resources to load ZOOMA DAOs from");
        }
        catch (IOException e) {
            getLog().warn("Failed to identify resources at 'zooma-annotation-dao.xml'");
        }

        XmlBeanDefinitionReader classPathBeansReader =
                new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory);
        classPathBeansReader.setBeanClassLoader(zoomaLoader);
        getLog().debug("Attempting to load bean definitions using dynamic class loader...");
        ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver(zoomaLoader);
        classPathBeansReader.setResourceLoader(resourceLoader);
        classPathBeansReader.loadBeanDefinitions("classpath*:zooma-annotation-dao.xml");
        getLog().debug("Bean definitions loaded ok!");

    }
}
