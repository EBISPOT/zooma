package uk.ac.ebi.fgpt.zooma.io;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertNotNull;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/06/14
 */
public class TestBeanFactoryPostProcessor {
    @Test
    public void testPostProcessing() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("test-app-context.xml");
        InjectableBean ib = ctx.getBean(InjectableBean.class);
        assertNotNull(ib.getInjectedBean());
    }
}
