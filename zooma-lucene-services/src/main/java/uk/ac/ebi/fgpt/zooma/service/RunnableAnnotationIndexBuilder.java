package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.index.IndexWriter;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;

import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 08/11/2013
 * Functional Genomics Group EMBL-EBI
 */
public class RunnableAnnotationIndexBuilder implements Runnable{

    private ZoomaLuceneIndexer indexer;
    private IndexWriter annotationWriter;
    private Collection<Annotation> annotations;
    private Map<URI, AnnotationProvenance> provenanceMap;

    public RunnableAnnotationIndexBuilder(ZoomaLuceneIndexer indexer, IndexWriter annotationWriter, Collection<Annotation> annotations, Map<URI, AnnotationProvenance> provenanceMap) {
        this.indexer = indexer;
        this.annotationWriter=annotationWriter;
        this.annotations = annotations;
        this.provenanceMap = provenanceMap;
    }

    @Override
    public void run() {
        try {
            indexer.createAnnotationIndex(annotations, provenanceMap, annotationWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
