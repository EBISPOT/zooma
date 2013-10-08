package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;

import java.util.Collection;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public class ArrayExpressAssayDAOTester {
    public static void main(String[] args) {
        System.setProperty("entityExpansionLimit", "10000000");
        ArrayExpressAssayDAOTester tester = new ArrayExpressAssayDAOTester();
        tester.printAllAnnotations();
    }

    private ArrayExpressAtlasEquivalentAnnotationsDAO aeAssayDAO;
    private ArrayExpressLoadingSession assayLoadingSession;

    public ArrayExpressAssayDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-ae-dao.xml");
        System.out.println("done!");
        aeAssayDAO = ctx.getBean("aeAtlasAssayEquivalenceDAO", ArrayExpressAtlasEquivalentAnnotationsDAO.class);
        assayLoadingSession = ctx.getBean("aeAssayLoadingSession", ArrayExpressAssayLoadingSession.class);
    }

    public void printAllAnnotations() {
        System.out.println("Reading assay annotations from ArrayExpress...");
        int start = 0;
        int size = 100000;
        int total = 3200000;
        int round = 1;

        while (start < total) {
            System.out.println("Doing round " + round++);
//            Collection<Annotation> annotations = aeAssayDAO.read(size, start);
            Collection<Annotation> annotations = aeAssayDAO.readByStudy(new SimpleStudy(null, "E-GEOD-6054"));

            for (Annotation a : annotations) {
                System.out.println("Next annotation: " + a.getURI());
            }
            start = start + size;

            System.out.println("Of the annotations supplied, there are " + annotations.size() +
                                       " annotations in arrayexpress that aligned to atlas");
            assayLoadingSession.clearCaches();
        }
        System.out.println("Done all increments!");
    }
}
