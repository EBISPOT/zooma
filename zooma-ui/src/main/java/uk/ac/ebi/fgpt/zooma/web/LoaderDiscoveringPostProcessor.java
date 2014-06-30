package uk.ac.ebi.fgpt.zooma.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.io.JarFileClassLoader;

import java.io.File;

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
@Component
public class LoaderDiscoveringPostProcessor implements BeanDefinitionRegistryPostProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // implement a class loader to load all jars in zooma.home
        File f = new File(System.getProperty("zooma.home"), "loaders");
        getLog().debug("Attempting to dynamically load loader modules from " + f.getAbsolutePath());
        ClassLoader zoomaLoader =
                new JarFileClassLoader(f, ((ConfigurableListableBeanFactory) registry).getBeanClassLoader());
        XmlBeanDefinitionReader classPathBeansReader = new XmlBeanDefinitionReader(registry);
        classPathBeansReader.setBeanClassLoader(zoomaLoader);
        getLog().debug("Attempting to load bean definitions using dynamic class loader...");
        classPathBeansReader.loadBeanDefinitions(new ClassPathResource("zooma-annotation-dao.xml", zoomaLoader));
        getLog().debug("Bean definitions loaded ok!");

    }
}
