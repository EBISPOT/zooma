import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.datasource.ExpressionAtlasAssayAnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.ExpressionAtlasAssayLoadingSession;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;

import java.util.Collection;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public class ExpressionAtlasAssayDAOTester {
    public static void main(String[] args) {
        System.setProperty("entityExpansionLimit", "10000000");
        ExpressionAtlasAssayDAOTester tester = new ExpressionAtlasAssayDAOTester();
        tester.printAllAnnotations();
    }

    private ExpressionAtlasAssayAnnotationDAO assayDAO;
    private ExpressionAtlasAssayLoadingSession assayLoadingSession;

    public ExpressionAtlasAssayDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-annotation-dao.xml");
        System.out.println("done!");
        assayDAO = ctx.getBean("expressionAtlasAssayDAO", ExpressionAtlasAssayAnnotationDAO.class);
        assayLoadingSession = ctx.getBean("expressionAtlasAssayLoadingSession", ExpressionAtlasAssayLoadingSession.class);
    }

    public void printAllAnnotations() {
        System.out.println("Reading assay annotations from ArrayExpress...");
        int start = 0;
        int size = 100000;
        int total = 3200000;
        int round = 1;

        while (start < total) {
            System.out.println("Doing round " + round++);
            Collection<Annotation> annotations = assayDAO.readByStudy(new SimpleStudy(null, "E-GEOD-6054"));

            for (Annotation a : annotations) {
                System.out.println("Next annotation: " + a.getURI());
            }
            start = start + size;

            System.out.println("Of the annotations supplied, there are " + annotations.size() +
                                       " annotations in arrayexpress that can be loaded into the atlas");
            assayLoadingSession.clearCaches();
        }
        System.out.println("Done all increments!");
    }
}
