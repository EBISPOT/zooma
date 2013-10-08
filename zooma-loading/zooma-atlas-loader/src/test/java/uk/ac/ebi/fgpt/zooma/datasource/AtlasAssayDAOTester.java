package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.util.Collection;

/**
 * Driver class that tests assay annotation loading from the Atlas
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class AtlasAssayDAOTester {
    public static void main(String[] args) {
        AtlasAssayDAOTester tester = new AtlasAssayDAOTester();
        tester.printAllAnnotations();
    }

    private AtlasAssayAnnotationDAO atlasAssayDAO;

    public AtlasAssayDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-atlas-dao.xml");
        System.out.println("done!");
        atlasAssayDAO = ctx.getBean("atlasAssayDAO", AtlasAssayAnnotationDAO.class);
    }

    public void printAllAnnotations() {
        System.out.print("Reading assay annotations from Atlas...");
        Collection<Annotation> annotations = atlasAssayDAO.read();
        System.out.println("done!");

        for (Annotation a : annotations) {
            System.out.println("Next annotation: " + a);
        }
        System.out.println("There are " + annotations.size() + " assay annotations in atlas");
    }
}
