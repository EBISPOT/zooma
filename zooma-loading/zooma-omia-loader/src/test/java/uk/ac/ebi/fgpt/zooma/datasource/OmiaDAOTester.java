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
 * @date 26/07/13
 */
public class OmiaDAOTester {
    public static void main(String[] args) {
        System.setProperty("zooma.home", "/tmp/zooma/omia-test");
        OmiaDAOTester tester = new OmiaDAOTester();
        tester.printAllAnnotations();
    }

    private OmiaAnnotationDAO omiaDAO;
    private OWLAPIAnnotationSerializer omiaSerializer;

    public OmiaDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-omia-dao.xml");
        System.out.println("done!");
        omiaDAO = ctx.getBean("omiaDAO", OmiaAnnotationDAO.class);
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
