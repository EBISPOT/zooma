package uk.ac.ebi.fgpt.zooma.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import uk.ac.ebi.fgpt.zooma.service.LuceneAnnotationSummarySearchService;
import uk.ac.ebi.fgpt.zooma.util.ZoomaSimilarity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 13/12/13
 */
public class ZoomaScoreCollector extends LuceneAnnotationSummarySearchService {
    public List<Float> getAllScores() throws IOException, InterruptedException {
        initOrWait();

        List<Float> allScores = new ArrayList<>();
        for (int i = 0; i < getReader().numDocs(); i++) {
            if (getReader().isDeleted(i)) {
                continue;
            }
            float nextScore = getMapper().mapDocument(getReader().document(i)).getQuality();
            allScores.add(nextScore);
        }
        Collections.sort(allScores);
        getLog().debug("Maximum score = " + Collections.max(allScores));
        return allScores;
    }

    public static void main(String[] args) {
        PrintStream out = null;
        try {
            ZoomaScoreCollector collector = new ZoomaScoreCollector();

            Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_36, Collections.emptySet());
            Similarity similarity = new ZoomaSimilarity();

            collector.setAnalyzer(analyzer);
            collector.setSimilarity(similarity);

            Directory annotationSummaryIndex = new NIOFSDirectory(new File(
                    System.getProperty("zooma.data.dir") + File.separator + "annotation_summary"));

            collector.setIndex(annotationSummaryIndex);

            if (args.length > 0) {
                out = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(args[0]))));
            }
            else {
                out = System.out;
            }

            List<Float> scores = collector.getAllScores();
            System.out.println("Printing out scores - min = " + Collections.min(scores) + "; max = " +
                                       Collections.max(scores));
            out.println("score");
            for (Float f : scores) {
                out.print(f);
                out.println();
            }
            out.println();
            out.flush();
        }
        catch (IOException e) {
            System.err.println("Failed to read from lucene indexes - (" + e.getMessage() + ")");
            e.printStackTrace();
            System.exit(1);
        }
        catch (InterruptedException e) {
            System.err.println("Failed to init score collector - (" + e.getMessage() + ")");
            e.printStackTrace();
            System.exit(1);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
