package uk.ac.ebi.fgpt.zooma.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Simon Jupp
 * @date 26/11/2013
 * Functional Genomics Group EMBL-EBI
 */
public class ZoomaCSVNamespaceHandler extends NamespaceHandlerSupport {
    @Override public void init() {
        registerBeanDefinitionParser("csvAnnotations", new CSVAnnotationsBeanDefinitionParser());
    }
}
