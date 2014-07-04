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
 * @date 04/07/14
 */
public class UniprotHumanDiseaseDAOTester {
    public static void main(String[] args) {
        System.setProperty("zooma.home", "/tmp/zooma/omim-test");
        UniprotHumanDiseaseDAOTester tester = new UniprotHumanDiseaseDAOTester();
        tester.printAllAnnotations();
    }

    private UniprotHumanDiseaseAnnotationDAO uniprotDAO;
    private OWLAPIAnnotationSerializer uniprotSerializer;

    public UniprotHumanDiseaseDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-uniprot-dao.xml");
        System.out.println("done!");
        uniprotDAO = ctx.getBean("uniprotDAO", UniprotHumanDiseaseAnnotationDAO.class);
        uniprotSerializer = ctx.getBean("annotationSerializer", OWLAPIAnnotationSerializer.class);
    }

    public void printAllAnnotations() {
        System.out.println("Number of annotations in Uniprot = " + uniprotDAO.count());

        System.out.print("Reading annotations from Uniprot...");
        Collection<Annotation> annotations = uniprotDAO.read();
        System.out.println("done!");

//        for (Annotation a : annotations) {
//            System.out.println("Next annotation: " + a);
//        }
        System.out.println("There are " + annotations.size() + " annotations in Uniprot");

        try {
            uniprotSerializer.serialize("uniprot", annotations, new File("uniprot-annotations.rdf"));
        }
        catch (ZoomaSerializationException e) {
            e.printStackTrace();
        }
    }

}
