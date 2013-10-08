package uk.ac.ebi.fgpt.zooma.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

public class TestLuceneIndexWriter {
    private Version version;

    private IndexSearcher searcher;
    private Analyzer analyzer;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Before
    public void setUp() {
        try {
            // create index setup
            version = Version.LUCENE_35;
            analyzer = new EnglishAnalyzer(version);

            // add some data to the index
            IndexWriterConfig config = new IndexWriterConfig(version, analyzer);
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
            IndexReader reader = IndexReader.open(index);
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

    @After
    public void tearDown() {
        try {
            searcher.close();
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
            Query q = new QueryParser(version, "title", analyzer).parse(querystr);

            // create a collector to obtain query results
            int hitsPerPage = 10;
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);

            // perform query
            getLog().debug("Performing lucene query for '" + querystr + "'");
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            // print results
            assertEquals("Wrong number of hits", 4, hits.length);
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
