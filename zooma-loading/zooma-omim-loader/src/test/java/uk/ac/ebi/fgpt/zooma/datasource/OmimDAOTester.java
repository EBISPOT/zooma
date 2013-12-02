package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.io.OWLAPIAnnotationSerializer;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.io.File;
import java.util.Collection;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimDAOTester {
    public static void main(String[] args) {
        System.setProperty("zooma.home", "/tmp/zooma/omim-test");
        OmimDAOTester tester = new OmimDAOTester();
        tester.printAllAnnotations();
    }

    private OmimAnnotationDAO omiaDAO;
    private OWLAPIAnnotationSerializer omiaSerializer;

    public OmimDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-omim-dao.xml");
        System.out.println("done!");
        omiaDAO = ctx.getBean("omimDAO", OmimAnnotationDAO.class);
        omiaSerializer = ctx.getBean("annotationSerializer", OWLAPIAnnotationSerializer.class);
    }

    public void printAllAnnotations() {
        System.out.print("Reading annotations from OMIA...");
        Collection<Annotation> annotations = omiaDAO.read();
        System.out.println("done!");

//        for (Annotation a : annotations) {
//            System.out.println("Next annotation: " + a);
//        }
        System.out.println("There are " + annotations.size() + " annotations in OMIA");

        try {
            omiaSerializer.serialize("omia", annotations, new File("omia-annotations.rdf"));
        }
        catch (ZoomaSerializationException e) {
            e.printStackTrace();
        }
    }

}
