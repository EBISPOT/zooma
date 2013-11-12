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
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationSummaryDAO;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.datasource.SparqlBiologicalEntityDAO;
import uk.ac.ebi.fgpt.zooma.model.*;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 17/05/2012 Functional Genomics Group EMBL-EBI
 *
 * This is an integration test of the DAOs when connected to a triple store, by default these should be
 * ignored unless a specific backend is provided
 *
 */
@Ignore
public class RepositoryConnectionTester extends TestCase {

    private Logger log = LoggerFactory.getLogger(getClass());

    private AnnotationDAO annotationBean;
    private AnnotationDAO annotationLuceneBean;
    private PropertyDAO propertyBean;
    private AnnotationSummaryDAO annotationSummaryBean;

    private SparqlBiologicalEntityDAO bioentityBean;

    boolean hasConnection = false;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("test-config.xml");
            SparqlService service = (SparqlService) context.getBean("lodeSparqlService");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            service.query("ASK {?s ?p ?o}", TupleQueryFormats.CSV.toString(), false, bos);

            String out = new String(bos.toByteArray(), "UTF-8");
            assertTrue(out.contains("true"));
            hasConnection = true;

            this.annotationBean = (AnnotationDAO) context.getBean("lodeAnnotationDAO");
            this.annotationLuceneBean = (AnnotationDAO) context.getBean("lodeLuceneAnnotationDAO");
            this.propertyBean = (PropertyDAO) context.getBean("lodePropertyDAO");
            this.annotationSummaryBean = (AnnotationSummaryDAO) context.getBean("lodeAnnotationSummaryDAO");
            this.bioentityBean = (SparqlBiologicalEntityDAO) context.getBean("lodeBiologicalEntityDAO");
        }
        catch (Exception e) {
            log.info("Failed to create beans for Repository connection test, no tests run");
        }
    }

    public void testSparqlAnnotationDao1 () {

        if (hasConnection) {
            log.info("testing annotation DAO, getting count...");
            assertTrue(annotationBean.count() > 0);
        }
    }

    public void testSparqlAnnotationDao2 () {

        if (hasConnection) {

            log.info("pulling out one annotation from zooma");
            List<Annotation> annotations = annotationBean.read(1,1);
            assertTrue(annotations.size() == 1);

            Annotation anno  = annotations.get(0);
            assertTrue(anno.getURI() != null);
            log.info("got annotation with URI: " + anno.getURI().toString());
            printAnotation(anno);

            Annotation sameAnno = annotationBean.read(anno.getURI());
            assertTrue(sameAnno.equals(anno));

            log.info("Getting biological entities");
            for (BiologicalEntity be : anno.getAnnotatedBiologicalEntities()) {
                printBiologicalEntity(be);
                for (Study s : be.getStudies()) {
                    printStudyEntity(s);
                }
            }
        }
    }

    public void testSparqlAnnotationDao2a () {

        if (hasConnection) {

            log.info("pulling out one annotation from zooma");

            Annotation anno  = annotationBean.read(URI.create("http://rdf.ebi.ac.uk/resource/zooma/owl/0002A0689524B81BD09367CFF3749369"));
            assertTrue(anno.getURI() != null);
            log.info("got annotation with URI: " + anno.getURI().toString());
            printAnotation(anno);

            Annotation sameAnno = annotationBean.read(anno.getURI());
            assertTrue(sameAnno.equals(anno));

            log.info("Getting biological entities");
            for (BiologicalEntity be : anno.getAnnotatedBiologicalEntities()) {
                printBiologicalEntity(be);
                for (Study s : be.getStudies()) {
                    printStudyEntity(s);
                }
            }
        }
    }

    public void testSparqlAnnotationDao3 () {

        if (hasConnection) {

            log.info("pulling out all annotations from zooma");

            long start = System.currentTimeMillis();
            for (Annotation annotation : annotationBean.read(100, 30000)) {
                System.out.println("annotation:  " + annotation.toString());
            }
            long end = System.currentTimeMillis();

            System.out.println("time: " + (end-start) / 60 );

        }
    }


    public void testSparqlSummaryAnnotationDao3 () {

        if (hasConnection) {

            log.info("pulling out all annotations summaries from zooma");

            for (AnnotationSummary summary:  annotationSummaryBean.read() ) {
                if (summary.getAnnotationURIs().size() > 1) {
                    System.out.println(summary.toString());
                }
            }

        }
    }

    public void testSparqlAnnotationDao5 () {

        if (hasConnection) {

            log.info("pulling out all lucene annotations from zooma");

            long start = System.currentTimeMillis();
            Collection<Annotation> annos = annotationLuceneBean.read();
            System.out.println("Lucene annos size: " + annos.size());
            for (Annotation annotation : annos) {
                System.out.println("annotation:  " + annotation.toString());
            }
            long end = System.currentTimeMillis();

            System.out.println("time: " + (end-start) / 60 );

        }
    }


    public void testSparqlAnnotationLoading ()  {

        if (hasConnection) {
            Study s = new SimpleStudy(URI.create("http://test.com/study2"), "study2", Collections.singleton(URI.create("http://test.com/study")));
            BiologicalEntity be = new SimpleBiologicalEntity(URI.create("http://test.com/target2"), "target2", URI.create("http://test.com/target"), s);
            Property p = new SimpleTypedProperty(URI.create("http://test.com/property2"), "propertype", "propertyvalue");
            AnnotationSource source = new SimpleAnnotationSource(URI.create("http://test.com/db2"), "db2", AnnotationSource.Type.DATABASE);
            AnnotationProvenance prov = new SimpleAnnotationProvenance(source, AnnotationProvenance.Evidence.MANUAL_CURATED, "zoomatest", new Date());

            Annotation newanno = new SimpleAnnotation(URI.create("http://test.com/annotation2"), Collections.singleton(be), p, prov, URI.create("http://ontology.com/term1"));

            annotationBean.create(newanno);
        }

    }

    public void testSparqlAnnotationDeleting ()  {

        if (hasConnection) {
            Study s = new SimpleStudy(URI.create("http://test.com/study2"), "study2", Collections.singleton(URI.create("http://test.com/study")));
            BiologicalEntity be = new SimpleBiologicalEntity(URI.create("http://test.com/target2"), "target2", URI.create("http://test.com/target"), s);
            Property p = new SimpleTypedProperty(URI.create("http://test.com/property2"), "propertype", "propertyvalue");
            AnnotationSource source = new SimpleAnnotationSource(URI.create("http://test.com/db2"), "db2", AnnotationSource.Type.DATABASE);
            AnnotationProvenance prov = new SimpleAnnotationProvenance(source, AnnotationProvenance.Evidence.MANUAL_CURATED, "zoomatest", new Date());

            Annotation newanno = new SimpleAnnotation(URI.create("http://test.com/annotation2"), Collections.singleton(be), p, prov, URI.create("http://ontology.com/term1"));

            annotationBean.delete(newanno);
        }

    }

    public void printAnotation(Annotation anno) {
        log.info("Annotation:" + anno.getURI());
        log.info("Property:" +
                ((TypedProperty) anno.getAnnotatedProperty()).getPropertyType() + " / " +
                ((TypedProperty) anno.getAnnotatedProperty()).getPropertyValue());
        log.info("Annnotations:\n");
        for (URI semanticTag : anno.getSemanticTags()) {
            log.info("\t" + semanticTag.toString());
        }

        Collection<BiologicalEntity> beSet = anno.getAnnotatedBiologicalEntities();

        for (BiologicalEntity be : beSet) {
            log.info("Bioentity: " + be.getURI().toString() + ":" + be.getName());
            for (Study study : be.getStudies()) {
                log.info("Study:" + study.getAccession() + " / " + study.getURI());
            }
        }
        printProvenance(anno.getProvenance());
    }

    public void printProvenance(AnnotationProvenance prov) {
        log.info("Annotation Provenance:");
        log.info("Annotator: " + prov.getAnnotator());
        log.info("Generator: " + prov.getGenerator());
        log.info("Evidence: " + prov.getEvidence().name());
        log.info("Accuracy: " + prov.getAccuracy().name());
        log.info("Source name: " + prov.getSource().getName());
        log.info("Source uri: " + prov.getSource().getURI());
        log.info("Source type: " + prov.getSource().getType());

        if (prov.getAnnotationDate() != null) {
            log.info("Annotation date: " + prov.getAnnotationDate().toString());
        }
        if (prov.getGeneratedDate() != null) {
            log.info("Generated date: " + prov.getGeneratedDate().toString());
        }

    }
//
    public void printBiologicalEntity(BiologicalEntity e) {
        log.info("Bio entity: " + e.getName() + "(URI=" + e.getURI().toString() + ")");
        if (e.getTypes().size() > 0) {
            for (URI bt : e.getTypes()) {
                log.info("bio entity of type: " + bt.toString());
            }
        }
    }

    public void printStudyEntity(Study e) {
        log.info("Study entity: " + e.getAccession() + "(URI=" + e.getURI().toString() + ")");
        if (e.getTypes().size() > 0) {
            for (URI bt : e.getTypes()) {
                log.info("Study of type: " + bt.toString());
            }
        }
    }
}
