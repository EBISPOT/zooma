package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.util.Collection;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/10/14
 */
public class CSVAnnotationDAOTester {
    public static void main(String[] args) {
        System.setProperty("entityExpansionLimit", "10000000");
        CSVAnnotationDAOTester tester = new CSVAnnotationDAOTester();
        tester.printAllAnnotations();
    }

    private CSVAnnotationDAO dao;

    public CSVAnnotationDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-annotation-dao.xml");
        System.out.println("done!");
        dao = ctx.getBean("csvAnnotationDAO", CSVAnnotationDAO.class);
    }

    public void printAllAnnotations() {
        System.out.println("Reading annotations from CSV...");
        Collection<Annotation> annotations = dao.read();
        System.out.println("done!");

        for (Annotation a : annotations) {
            System.out.println("Next annotation: " + a);
        }
        System.out.println("There are " + annotations.size() + "  annotations in the ChEMBL database");
    }
}
