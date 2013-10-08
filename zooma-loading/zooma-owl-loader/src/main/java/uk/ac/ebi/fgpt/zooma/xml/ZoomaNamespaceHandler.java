package uk.ac.ebi.fgpt.zooma.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * An implementation of Spring's NamespaceHandlerSupport class that allows ZOOMA OWLAnnotationDAOs to be generated with
 * a simple namespace configuration element
 *
 * @author Tony Burdett
 * @date 23/05/13
 */
public class ZoomaNamespaceHandler extends NamespaceHandlerSupport {
    @Override public void init() {
        registerBeanDefinitionParser("owlAnnotations", new OWLAnnotationsBeanDefinitionParser());
    }
}
