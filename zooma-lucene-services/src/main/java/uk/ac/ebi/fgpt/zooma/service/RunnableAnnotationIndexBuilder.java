package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.index.IndexWriter;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.io.IOException;
import java.util.List;

/**
 * @author Simon Jupp
 * @date 08/11/2013
 * Functional Genomics Group EMBL-EBI
 */
public class RunnableAnnotationIndexBuilder implements Runnable{

    private int limit;
    private int offset;
    private ZoomaLuceneIndexer indexer;
    private AnnotationDAO dao;
    private IndexWriter annotationWriter;

    public RunnableAnnotationIndexBuilder(int limit, int offset, ZoomaLuceneIndexer indexer, IndexWriter annotationWriter, AnnotationDAO dao) {
        this.limit = limit;
        this.offset = offset;
        this.indexer = indexer;
        this.dao = dao;
        this.annotationWriter=annotationWriter;
    }

    @Override
    public void run() {
        try {
            List<Annotation> as = dao.read(limit, offset);
            indexer.createAnnotationIndex(as, annotationWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
