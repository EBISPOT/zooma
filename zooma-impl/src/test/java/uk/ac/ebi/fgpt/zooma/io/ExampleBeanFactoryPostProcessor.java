package uk.ac.ebi.fgpt.zooma.io;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;

import static org.junit.Assert.fail;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/06/14
 */
@Component
public class ExampleBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("Starting loading additional definitions...");

        // implement a class loader to load all jars in zooma.home
//        File f = FileSystems.getDefault().getPath("target", "test-classes").toFile();
//        ClassLoader zoomaLoader = null;
//        try {
//            zoomaLoader = new URLClassLoader(new URL[]{f.toURI().toURL()});
//        }
//        catch (MalformedURLException e) {
//            e.printStackTrace();
//            fail();
//        }
//        XmlBeanDefinitionReader classPathBeansReader = new XmlBeanDefinitionReader((BeanDefinitionRegistry)beanFactory);
//        classPathBeansReader.setBeanClassLoader(zoomaLoader);
//        classPathBeansReader.loadBeanDefinitions(new ClassPathResource("dynamic-app-context.xml", zoomaLoader));

        XmlBeanDefinitionReader classPathBeansReader =
                new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory);
        classPathBeansReader.loadBeanDefinitions(new ClassPathResource("dynamic-app-context.xml"));


        System.out.println("Finished loading additional definitions");
    }
}
