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

    private ChemblEFOAnnotationDAO chemblEFODAO;
    private ChemblCLOAnnotationDAO chemblCLODAO;

    public ChemblDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-chembl-dao.xml");
        System.out.println("done!");
//        chemblEFODAO = ctx.getBean("chemblEFODAO", ChemblEFOAnnotationDAO.class);
        chemblCLODAO = ctx.getBean("chemblCLODAO", ChemblCLOAnnotationDAO.class);

    }

    public void printAllAnnotations() {
        System.out.print("Reading annotations from the ChEMBL database...");
//        Collection<Annotation> annotations = chemblEFODAO.read();
//        System.out.println("done!");
//
//        for (Annotation a : annotations) {
//            System.out.println("Next annotation: " + a);
//        }
//        System.out.println("There are " + annotations.size() + " EFO annotations in the ChEMBL database");

        Collection<Annotation> cloAnnotations = chemblCLODAO.read();
        System.out.println("done!");

        for (Annotation a : cloAnnotations) {
            System.out.println("Next annotation: " + a);
        }
        System.out.println("There are " + cloAnnotations.size() + " CLO annotations in the ChEMBL database");
    }
}
