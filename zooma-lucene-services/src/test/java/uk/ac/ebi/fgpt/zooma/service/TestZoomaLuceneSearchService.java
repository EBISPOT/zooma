package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.fgpt.zooma.datasource.ZoomaDAO;
import uk.ac.ebi.fgpt.zooma.model.Identifiable;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestZoomaLuceneSearchService {
    private Version version;
    private Analyzer analyzer;

    private ZoomaLuceneSearchService searchService;
    private ZoomaDAO<Identifiable> dao;

    private Identifiable foo;
    private Identifiable bar;
    private Identifiable baz;

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
            addDoc(w, "foo");
            addDoc(w, "bar");
            addDoc(w, "baz");
            addDoc(w, "foo bar");
            addDoc(w, "foo bar baz");
            addDoc(w, "bar baz");
            addDoc(w, "foo baz");
            w.close();

            // create a new zooma search service
            searchService = new ZoomaLuceneSearchService() {
            };
            searchService.setIndex(index);
            searchService.init();

            foo = mock(Identifiable.class);
            bar = mock(Identifiable.class);
            baz = mock(Identifiable.class);

            // create a mocked zooma dao
            dao = (ZoomaDAO<Identifiable>) mock(ZoomaDAO.class);
            when(dao.read(convertToURI("foo"))).thenReturn(foo);
            when(dao.read(convertToURI("bar"))).thenReturn(bar);
            when(dao.read(convertToURI("baz"))).thenReturn(baz);
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void addDoc(IndexWriter w, String s) {
        try {
            Document doc = new Document();
            doc.add(new Field("name", s, Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("uri", convertToURI(s).toString(), Field.Store.YES, Field.Index.ANALYZED));
            w.addDocument(doc);
        }
        catch (CorruptIndexException e) {
            e.printStackTrace();
            fail();
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    public URI convertToURI(String s) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(s, "UTF-8");
        encoded = encoded.replaceAll("%20", "+");
        return URI.create("http://www.ebi.ac.uk/zooma/test/" + encoded);
    }

    @Test
    public void testDoQueryString() {
        // build a query
        String querystr = "foo";
        try {
            Query q = new QueryParser(version, "name", analyzer).parse(querystr);
            Collection<String> results = searchService.doQuery(q, new SingleFieldStringMapper("name"));
            assertEquals("Wrong number of results", 4, results.size());
            assertTrue(results.contains("foo"));
            assertTrue(results.contains("foo bar"));
            assertTrue(results.contains("foo bar baz"));
            assertTrue(results.contains("foo baz"));

            q = new QueryParser(version, "uri", analyzer).parse(querystr);
            results = searchService.doQuery(q, new SingleFieldStringMapper("uri"));
            assertEquals("Wrong number of results", 4, results.size());
            assertTrue(results.contains(convertToURI("foo").toString()));
            assertTrue(results.contains(convertToURI("foo bar").toString()));
            assertTrue(results.contains(convertToURI("foo bar baz").toString()));
            assertTrue(results.contains(convertToURI("foo baz").toString()));
        }
        catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testDoQueryObject() {
        // build a query
        try {
            String querystr = "foo";
            Query q = new QueryParser(version, "uri", analyzer).parse(querystr);
            Collection<Identifiable> results = searchService.doQuery(q, new SingleFieldURIMapper("uri"), dao);
            assertEquals("Wrong number of results", 1, results.size());
            assertTrue("Identifiable 'foo' not found", results.contains(foo));

            querystr = "bar";
            q = new QueryParser(version, "uri", analyzer).parse(querystr);
            results = searchService.doQuery(q, new SingleFieldURIMapper("uri"), dao);
            assertEquals("Wrong number of results", 1, results.size());
            assertTrue("Identifiable 'bar' not found", results.contains(bar));

            querystr = "baz";
            q = new QueryParser(version, "uri", analyzer).parse(querystr);
            results = searchService.doQuery(q, new SingleFieldURIMapper("uri"), dao);
            assertEquals("Wrong number of results", 1, results.size());
            assertTrue("Identifiable 'baz' not found", results.contains(baz));
        }
        catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testDoQueryForSubstring() {
        String field = "name";
        String searchString = "foo";

        // tokenize the pattern using the given analyzer
        List<String> terms = new ArrayList<>();
        TokenStream stream = analyzer.tokenStream(field, new StringReader(QueryParser.escape(searchString)));
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
        try {
            while (stream.incrementToken()) {
                terms.add(termAtt.toString());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Can't parse search string");
        }

        Query q;
        if (terms.size() == 1) {
            // create a simple term query
            q = new TermQuery(new Term(field, terms.get(0)));
        }
        else {
            // create a span query
            List<SpanQuery> stqs = new ArrayList<>();
            for (String term : terms) {
                stqs.add(new SpanTermQuery(new Term(field, term)));
            }
            q = new SpanNearQuery(stqs.toArray(new SpanQuery[stqs.size()]), 1, false);
        }

        Collection<String> results;
        try {
            results = searchService.doQuery(q, new SingleFieldStringMapper(field));
            for (String s : results) {
                System.out.println("Next result: '" + s + "'");
            }
            assertEquals("Wrong number of results", 4, results.size());
            assertTrue(results.contains("foo"));
            assertTrue(results.contains("foo bar"));
            assertTrue(results.contains("foo bar baz"));
            assertTrue(results.contains("foo baz"));
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Query failed");
        }
    }
}
