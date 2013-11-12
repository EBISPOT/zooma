package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.index.IndexWriter;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author Simon Jupp
 * @date 08/11/2013
 * Functional Genomics Group EMBL-EBI
 */
public class RunnableAnnotationIndexBuilder implements Runnable{

    private ZoomaLuceneIndexer indexer;
    private IndexWriter annotationWriter;
    private Collection<Annotation> annotations;

    public RunnableAnnotationIndexBuilder(ZoomaLuceneIndexer indexer, IndexWriter annotationWriter, Collection<Annotation> annotations) {
        this.indexer = indexer;
        this.annotationWriter=annotationWriter;
        this.annotations = annotations;
    }

    @Override
    public void run() {
        try {
            indexer.createAnnotationIndex(annotations, annotationWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
