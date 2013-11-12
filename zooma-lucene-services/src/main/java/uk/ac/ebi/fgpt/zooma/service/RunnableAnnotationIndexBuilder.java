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
    private AnnotationDAO dao;
    private IndexWriter annotationWriter;
    private Collection<URI> annotationURIs;

    public RunnableAnnotationIndexBuilder(ZoomaLuceneIndexer indexer, IndexWriter annotationWriter, AnnotationDAO dao, Collection<URI> annotationURIs) {
        this.indexer = indexer;
        this.dao = dao;
        this.annotationWriter=annotationWriter;
        this.annotationURIs = annotationURIs;
    }

    @Override
    public void run() {
        try {
            Collection<Annotation> annos = new HashSet<>();
            for (URI aUri : annotationURIs) {
                annos.add(dao.read(aUri));
            }
            indexer.createAnnotationIndex(annos, annotationWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
