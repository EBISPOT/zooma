package uk.ac.ebi.fgpt;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.datasource.OWLAnnotationDAO;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.io.OWLAPIAnnotationSerializer;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.io.File;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA. Author: James Malone Date: 07/11/12 Time: 14:22 To change this template use File |
 * Settings | File Templates.
 */
public class OWLDAOTester {
    public static void main(String[] args) {
        System.setProperty("entityExpansionLimit", "10000000");
        System.out.println("Beginning owl dao test");
        OWLDAOTester tester = new OWLDAOTester();
        tester.printAllAnnotations();
        tester.serializeAllAnnotations();
    }

    private OWLAnnotationDAO OWLDAO;
    private OWLAPIAnnotationSerializer serializer;

    public OWLDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-annotation-dao.xml");
        System.out.println("done!");
        OWLDAO = ctx.getBean("test-owlAnnotationDAO", OWLAnnotationDAO.class);
        serializer = ctx.getBean("annotationSerializer", OWLAPIAnnotationSerializer.class);
    }

    public void printAllAnnotations() {
        System.out.print("Reading annotations from the OWL file...");
        Collection<Annotation> annotations = OWLDAO.read();
        System.out.println("done");

        for (Annotation a : annotations) {
            System.out.println("Next annotation: " + a);
        }
        System.out.println("There are " + annotations.size() + " annotations in the OWL file");
    }

    public void serializeAllAnnotations() {
        System.out.print("Reading annotations from the OWL file...");
        Collection<Annotation> annotations = OWLDAO.read();
        System.out.println("done");

        try {
            File f = new File("annotations.owl");
            System.out.print("Serializing " + annotations.size() + " to " + f.getAbsolutePath() + "...");
            serializer.serialize("test", annotations, f);
            System.out.println("done!");
        }
        catch (ZoomaSerializationException e) {
            System.err.println("Failed to serialize annotations (" + e.getMessage() + ")");
            e.printStackTrace();
        }
    }
}
