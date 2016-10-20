package uk.ac.ebi.spot.builders;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import uk.ac.ebi.spot.services.CSVLoader;
import uk.ac.ebi.spot.services.CSVLoadingSession;
import uk.ac.ebi.spot.services.DefaultAnnotationFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An implementation of the BeanDefinitionRegistryPostProcessor allowing registration of further bean definitions
 * Creates a {@link CSVLoader} bean and its corresponding {@link uk.ac.ebi.spot.datasource.AnnotationLoadingSession} and
 * {@link uk.ac.ebi.spot.datasource.AnnotationFactory} beans, for each loader defined in the application.properties file
 *
 * Beans will be added to the classpath so they can be accessed through an autowired Collection of {CSVLoader}s
 *
 * Created by olgavrou on 11/08/2016.
 */
@Configuration
public class LoaderBeanBuilder implements BeanDefinitionRegistryPostProcessor {


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {

        Resource resource = new ClassPathResource("application.properties");
        Properties properties = null;
        try {
            properties = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            throw new RuntimeException("Could not load application.properties");
        }

        int numLoaders = Integer.valueOf((String) properties.get("loaders.count"));

        //for each loader found in the properties file, create beans
        for (int i = 1; i <= numLoaders; i++){
            String uri = (String) properties.get("uri." + i);
            String name = (String) properties.get("name." + i);
            String delimeter = (String) properties.get("delimeter." + i);
            String loadFrom = (String) properties.get("loadFrom." + i);

            RootBeanDefinition loadingSessionBean = new RootBeanDefinition(CSVLoadingSession.class);
            Map<String, String> loadingSessionValues = new HashMap<>();
            loadingSessionValues.put("name", name);
            loadingSessionValues.put("uri", uri);
            loadingSessionBean.setPropertyValues(new MutablePropertyValues(loadingSessionValues));
            loadingSessionBean.setInitMethodName("init");
            beanDefinitionRegistry.registerBeanDefinition(name + "LoadingSession", loadingSessionBean);

            RootBeanDefinition annotationFactoryBean = new RootBeanDefinition(DefaultAnnotationFactory.class);
            Map<String, Object> annotationFactoryValues = new HashMap<>();
            annotationFactoryValues.put("annotationLoadingSession", loadingSessionBean);
            annotationFactoryBean.setPropertyValues(new MutablePropertyValues(annotationFactoryValues));
            beanDefinitionRegistry.registerBeanDefinition(name + "AnnotationFactory", annotationFactoryBean);

            RootBeanDefinition csvLoaderBean = new RootBeanDefinition(CSVLoader.class);
            Map<String, Object> loaderValues = new HashMap<>();
            loaderValues.put("loadFrom", loadFrom);
            loaderValues.put("delimiter", delimeter);
            loaderValues.put("annotationFactory", annotationFactoryBean);
            csvLoaderBean.setPropertyValues(new MutablePropertyValues(loaderValues));
            beanDefinitionRegistry.registerBeanDefinition(name + "CSVLoader", csvLoaderBean);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

}
