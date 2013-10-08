package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.util.Collection;

/**
 * Driver class that tests sample annotation loading from the Atlas
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class AtlasSampleDAOTester {
    public static void main(String[] args) {
        AtlasSampleDAOTester tester = new AtlasSampleDAOTester();
        tester.printAllAnnotations();
    }

    private AtlasSampleAnnotationDAO atlasSampleDAO;

    public AtlasSampleDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-atlas-dao.xml");
        System.out.println("done!");
        atlasSampleDAO = ctx.getBean("atlasSampleDAO", AtlasSampleAnnotationDAO.class);
    }

    public void printAllAnnotations() {
        System.out.print("Reading sample annotations from Atlas...");
        Collection<Annotation> annotations = atlasSampleDAO.read();
        System.out.println("done!");

        for (Annotation a : annotations) {
            System.out.println("Next annotation: " + a);
        }
        System.out.println("There are " + annotations.size() + " sample annotations in atlas");
    }
}
