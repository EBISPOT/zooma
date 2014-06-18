package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.util.Collection;

/**
 * Created by dwelter on 28/05/14.
 */
public class ChemblDAOTester {

    public static void main(String[] args) {
        ChemblDAOTester tester = new ChemblDAOTester();
        tester.printAllAnnotations();
    }

    private ChemblAnnotationDAO chemblDAO;

    public ChemblDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-chembl-dao.xml");
        System.out.println("done!");
        chemblDAO = ctx.getBean("chemblDAO", ChemblAnnotationDAO.class);

    }

    public void printAllAnnotations() {
        System.out.print("Reading annotations from the ChEMBL database...");
        Collection<Annotation> annotations = chemblDAO.read();
        System.out.println("done!");

        for (Annotation a : annotations) {
            System.out.println("Next annotation: " + a);
        }
        System.out.println("There are " + annotations.size() + "  annotations in the ChEMBL database");   
    }
}
