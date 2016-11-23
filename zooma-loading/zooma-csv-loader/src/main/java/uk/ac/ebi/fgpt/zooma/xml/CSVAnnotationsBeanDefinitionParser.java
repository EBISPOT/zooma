package uk.ac.ebi.fgpt.zooma.xml;

import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.zooma.datasource.CSVAnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.CSVLoadingSession;
import uk.ac.ebi.fgpt.zooma.datasource.DefaultAnnotationFactory;

import java.net.URI;

/**
 * An implementation of Spring's BeanDefinitionParser class that generates a fully preconfigured OWLAnnotationDAO by
 * parsing a simple bean definition
 *
 * @author Simon Jupp
 * @date 26/11/2013 Functional Genomics Group EMBL-EBI
 */
public class CSVAnnotationsBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }


    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        // required (non-null) attributes
        String uri = element.getAttribute("uri");
        String name = element.getAttribute("name");

        // optional attributes
        Resource csvResource = null;
        String loadFrom = element.getAttribute("loadFrom");
        if (StringUtils.hasText(loadFrom)) {
            csvResource = new DefaultResourceLoader().getResource(loadFrom);
        }
        else {
            csvResource = new DefaultResourceLoader().getResource(uri);
        }

        String annotationCreator = null;
        if (element.hasAttribute("annotationCreator")) {
            annotationCreator = element.getAttribute("annotationCreator");
        }

        String delimiter = null;
        if (element.hasAttribute("delimiter")) {
            delimiter = element.getAttribute("delimiter");
        }

        String defaultTargetType = null;
        if (element.hasAttribute("defaultTargetType")) {
            defaultTargetType = element.getAttribute("defaultTargetType");
        }

        String defaultSourceType = null;
        if (element.hasAttribute("defaultSourceType")) {
            defaultSourceType = element.getAttribute("defaultSourceType");
        }

        // creating loading session bean
        BeanDefinitionBuilder csvLoadingSession = BeanDefinitionBuilder.rootBeanDefinition(CSVLoadingSession.class);
        csvLoadingSession.addConstructorArgValue(uri);
        csvLoadingSession.addConstructorArgValue(name);
        if (defaultTargetType != null && defaultSourceType != null) {
            csvLoadingSession.addConstructorArgValue(URI.create(defaultTargetType));
            csvLoadingSession.addConstructorArgValue(URI.create(defaultSourceType));
        }

        parserContext.registerBeanComponent(new BeanComponentDefinition(csvLoadingSession.getBeanDefinition(),
                                                                        name + "-csvLoader"));

        // create annotation factory bean
        BeanDefinitionBuilder csvAnnotationFactory =
                BeanDefinitionBuilder.rootBeanDefinition(DefaultAnnotationFactory.class);
        csvAnnotationFactory.addConstructorArgReference(name + "-csvLoader");
        parserContext.registerBeanComponent(new BeanComponentDefinition(csvAnnotationFactory.getBeanDefinition(),
                                                                        name + "-csvFactory"));

        // create the csv DAO
        BeanDefinitionBuilder csvAnnotationDao = BeanDefinitionBuilder.rootBeanDefinition(CSVAnnotationDAO.class);
        csvAnnotationDao.setInitMethodName("init");
        csvAnnotationDao.addConstructorArgReference(name + "-csvFactory");
        csvAnnotationDao.addConstructorArgValue(csvResource);
        csvAnnotationDao.addPropertyReference("olsSearchService", "olsSearchService");
        if (delimiter != null) {
            csvAnnotationDao.addConstructorArgValue(delimiter);
        }

        parserContext.registerBeanComponent(new BeanComponentDefinition(csvAnnotationDao.getBeanDefinition(),
                                                                        name + "-csvDAO"));

        return null;
    }
}
