package uk.ac.ebi.fgpt;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.datasource.OWLAnnotationDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

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
    }

    private OWLAnnotationDAO OWLDAO;

    public OWLDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-annotation-dao.xml");
        System.out.println("done!");
        OWLDAO = ctx.getBean("efo-owlAnnotationDAO", OWLAnnotationDAO.class);
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
}
