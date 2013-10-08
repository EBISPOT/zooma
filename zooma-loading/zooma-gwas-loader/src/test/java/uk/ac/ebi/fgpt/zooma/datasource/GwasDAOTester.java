package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: daniwelter
 * Date: 06/11/2012
 * Time: 16:45
 * To change this template use File | Settings | File Templates.
 */
public class GwasDAOTester {

    public static void main(String[] args) {
        GwasDAOTester tester = new GwasDAOTester();
        tester.printAllAnnotations();
    }

    private GwasAnnotationDAO gwasDAO;

    public GwasDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-gwas-dao.xml");
        System.out.println("done!");
        gwasDAO = ctx.getBean("gwasDAO", GwasAnnotationDAO.class);
    }

    public void printAllAnnotations() {
        System.out.print("Reading annotations from the GWAS catalogue...");
        Collection<Annotation> annotations = gwasDAO.read();
        System.out.println("done!");

        for (Annotation a : annotations) {
            System.out.println("Next annotation: " + a);
        }
        System.out.println("There are " + annotations.size() + " annotations in the GWAS catalogue");
    }
}
