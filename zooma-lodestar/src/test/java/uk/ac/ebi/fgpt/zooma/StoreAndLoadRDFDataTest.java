package uk.ac.ebi.fgpt.zooma;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.lode.service.SparqlService;
import uk.ac.ebi.fgpt.lode.utils.TupleQueryFormats;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Ignore
public class StoreAndLoadRDFDataTest extends TestCase {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private SparqlService sparqlService;
    private AnnotationDAO annotationBean;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            logger.trace("Setup 1");
            ApplicationContext context = new ClassPathXmlApplicationContext("minimal-virtuoso-test-config.xml");
            logger.trace("Setup 2");
            sparqlService = (SparqlService) context.getBean("lodeSparqlService");
            annotationBean = (AnnotationDAO) context.getBean("lodeAnnotationDAO");
        } catch (Exception e) {
            logger.error("Failed to create beans for Repository connection test, no tests run");
            fail();
        }
    }

    public void testSimpleSPARQLQuery() {
        try {
            StringBuilder queryStringBuilder = new StringBuilder("prefix ns: <http://www.openannotation.org/ns/>");
            queryStringBuilder.append("SELECT * WHERE {?ind ns:generated ?date .}");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            sparqlService.query(queryStringBuilder.toString(), TupleQueryFormats.CSV.toString(), 0, -1, false,
                    baos);
            System.out.println(baos.toString());
            assertTrue(true);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            fail();
        }
    }

    public void testReadAnnotations() {
        List<Annotation> annotations = annotationBean.read(-1, 0);
        assertTrue(annotations.size() > 1);

    }
}
