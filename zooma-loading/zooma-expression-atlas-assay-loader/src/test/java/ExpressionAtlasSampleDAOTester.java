import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.datasource.ExpressionAtlasLoadingSession;
import uk.ac.ebi.fgpt.zooma.datasource.ExpressionAtlasSampleAnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.ExpressionAtlasSampleLoadingSession;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;

import java.util.Collection;

/**
 * Driver class that tests sample annotation loading from arrayexpress
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class ExpressionAtlasSampleDAOTester {
    public static void main(String[] args) {
        System.setProperty("entityExpansionLimit", "10000000");
        ExpressionAtlasSampleDAOTester tester = new ExpressionAtlasSampleDAOTester();
        tester.printAllAnnotations();
    }

    private ExpressionAtlasSampleAnnotationDAO aeSampleDAO;
    private ExpressionAtlasLoadingSession assayLoadingSession;

    public ExpressionAtlasSampleDAOTester() {
        System.out.print("Loading spring context...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:zooma-annotation-dao.xml");
        System.out.println("done!");
        aeSampleDAO = ctx.getBean("expressionAtlasSampleDAO", ExpressionAtlasSampleAnnotationDAO.class);
        assayLoadingSession = ctx.getBean("expressionAtlasSampleLoadingSession", ExpressionAtlasSampleLoadingSession.class);
    }

    public void printAllAnnotations() {
        System.out.println("Reading sample annotations from arrayexpress...");
        int start = 0;
        int size = 100000;
        int total = 7600000;
        int round = 1;

        while (start < total) {
            System.out.println("Doing round " + round++);
//            Collection<Annotation> annotations = aeSampleDAO.read(size, start);
            Collection<Annotation> annotations = aeSampleDAO.readByStudy(new SimpleStudy(null, "E-GEOD-6054"));

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
