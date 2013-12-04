package uk.ac.ebi.fgpt.zooma.xml;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.zooma.datasource.CSVAnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.CSVAnnotationFactory;
import uk.ac.ebi.fgpt.zooma.datasource.CSVLoadingSession;

import java.io.IOException;
import java.net.URI;

/**
 * An implementation of Spring's BeanDefinitionParser class that generates a fully preconfigured OWLAnnotationDAO by
 * parsing a simple bean definition
 * @author Simon Jupp
 * @date 26/11/2013
 * Functional Genomics Group EMBL-EBI
 */
public class CSVAnnotationsBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }


    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

        // required
        String datasourceUrl = element.getAttribute("datasourceUrl");
        String datasourceName = element.getAttribute("datasourceName");

        // optional
        String delimiter = null;
        if (element.hasAttribute("delimiter")) {
            delimiter = element.getAttribute("delimiter");
        }
        String namespace = null;
        if (element.hasAttribute("namespace")) {
            namespace = element.getAttribute("namespace");
        }
        String namespacePrefix = null;
        if (element.hasAttribute("namespacePrefix")) {
            namespacePrefix = element.getAttribute("namespacePrefix");
        }

        if (element.hasAttribute("file") && element.hasAttribute("url")) {
            throw new BeanDefinitionValidationException("File path and URL specified for " + datasourceName + " csv loader, you must use one or the other");
        }

        // creating loading session bean
        BeanDefinitionBuilder csvLoadingSession = BeanDefinitionBuilder.rootBeanDefinition(CSVLoadingSession.class);
        if (namespace!= null) {
            csvLoadingSession.addConstructorArgValue(namespace);
        }
        if (namespacePrefix!=null) {
            csvLoadingSession.addConstructorArgValue(namespacePrefix);
        }
        else {
            csvLoadingSession.addConstructorArgValue(datasourceName);
        }

        if (element.hasAttribute("defaultTargetType") || element.hasAttribute("defaultSourceType")) {
            String targetUri = element.hasAttribute("defaultTargetType") ? element.getAttribute("defaultTargetType") : null;
            String studyUri = element.hasAttribute("defaultSourceType") ? element.getAttribute("defaultSourceType") : null;
            csvLoadingSession.addConstructorArgValue(URI.create(targetUri));
            csvLoadingSession.addConstructorArgValue(URI.create(studyUri));
        }


        parserContext.registerBeanComponent(new BeanComponentDefinition(csvLoadingSession.getBeanDefinition(),
                datasourceName + "-csvLoader"));

        // create annotation factory bean
        BeanDefinitionBuilder csvAnnotationFactory = BeanDefinitionBuilder.rootBeanDefinition(CSVAnnotationFactory.class);
        csvAnnotationFactory.addConstructorArgValue(datasourceUrl);
        csvAnnotationFactory.addConstructorArgValue(datasourceName);
        csvAnnotationFactory.addConstructorArgReference(datasourceName + "-csvLoader");
        parserContext.registerBeanComponent(new BeanComponentDefinition(csvAnnotationFactory.getBeanDefinition(),
                datasourceName + "-csvFactory"));

        // create the csv DAO
        BeanDefinitionBuilder csvAnnotationDao = BeanDefinitionBuilder.rootBeanDefinition(CSVAnnotationDAO.class);

        if (element.hasAttribute("file")) {
            Resource fileResource = new DefaultResourceLoader().getResource(element.getAttribute("file"));
            try {
                csvAnnotationDao.addConstructorArgValue(fileResource.getFile());
            } catch (IOException e) {
                throw new BeanDefinitionValidationException("Couldn't find CSV file " + element.getAttribute("file"), e);
            }
        }

        if (element.hasAttribute("url")) {
            Resource urlResource = new DefaultResourceLoader().getResource(element.getAttribute("url"));
            try {
                csvAnnotationDao.addConstructorArgValue(urlResource.getURL());
            } catch (IOException e) {
                throw new BeanDefinitionValidationException("Couldn't access URL to CSV file " + element.getAttribute("url"), e);
            }
        }

        csvAnnotationDao.addConstructorArgValue(datasourceName);

        if (delimiter != null) {
            csvAnnotationDao.addConstructorArgValue(delimiter);
        }

        csvAnnotationDao.addPropertyReference("annotationFactory", datasourceName + "-csvFactory");


        parserContext.registerBeanComponent(new BeanComponentDefinition(csvAnnotationDao.getBeanDefinition(),
                datasourceName + "-csvDAO"));

        return null;

    }

}
