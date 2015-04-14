package uk.ac.ebi.fgpt.zooma.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * A custom configurator that performs some Spring config tweaks required by the ZOOMA web application.  This class
 * prevents URLs that are passed as web requests being decoded, and registers a {@link ZoomaModule} to customize aspects
 * of JSON serialization
 *
 * @author Tony Burdett
 * @date 15/07/13
 */
@Component
public class ZoomaWebConfigurator implements BeanPostProcessor {
    private ZoomaModule zoomaModule;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZoomaModule getZoomaModule() {
        return zoomaModule;
    }

    @Autowired
    public void setZoomaModule(ZoomaModule zoomaModule) {
        this.zoomaModule = zoomaModule;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        // configure jackson JSON setup
        if (o instanceof MappingJackson2HttpMessageConverter) {
            // create object mapper
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(getZoomaModule());

            // customize the HttpMessageConverter
            getLog().debug("Customizing HttpMessageConverter '" + s + "' to perform custom JSON serialization");
            MappingJackson2HttpMessageConverter messageConverter = (MappingJackson2HttpMessageConverter) o;
            messageConverter.setObjectMapper(objectMapper);
        }

        // configure URL decoding properties
        if (o instanceof RequestMappingHandlerMapping) {
            getLog().debug("Customizing HandlerMapping '" + s + "' to prevent request mapping url decoding");
            RequestMappingHandlerMapping mapping = (RequestMappingHandlerMapping) o;
            mapping.setUrlDecode(false);
            mapping.setUseSuffixPatternMatch(false);
        }
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        return o;
    }
}
