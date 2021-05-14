package uk.ac.ebi.fgpt.zooma.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestLuceneIndexWriter {
    private Version version;

    private IndexSearcher searcher;
    private Analyzer analyzer;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @BeforeEach
    public void setUp() {
        try {
            // create index setup
            analyzer = new EnglishAnalyzer();

            // add some data to the index
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            Directory index = new RAMDirectory();
            IndexWriter w = new IndexWriter(index, config);
            addDoc(w, "Lucene in Action");
            addDoc(w, "Lucene for Dummies");
            addDoc(w, "Managing Gigabytes");
            addDoc(w, "The Art of Computer Science");
            addDoc(w, "Tony's Special Book on Lucene");
            addDoc(w, "Lots of lucenes");
            w.close();

            // create a searcher that can search this index
            IndexReader reader = DirectoryReader.open(index);
            searcher = new IndexSearcher(reader);
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void addDoc(IndexWriter w, String s) {
        try {
            Document doc = new Document();
            doc.add(new Field("title", s, Field.Store.YES, Field.Index.ANALYZED));
            w.addDocument(doc);
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testQuery() {
        try {
            // build a query
            String query = "luc";
            String querystr = query + "*";
            Query q = new QueryParser("title", analyzer).parse(querystr);

            // create a collector to obtain query results
            int hitsPerPage = 10;
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);

            // perform query
            getLog().debug("Performing lucene query for '" + querystr + "'");
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            // print results
            assertEquals(4, hits.length, "Wrong number of hits");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                String result = d.get("title");
                assertTrue(result.toLowerCase().contains(query));
                getLog().debug("Result " + (i + 1) + ": " + d.get("title"));
            }
        }
        catch (ParseException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
