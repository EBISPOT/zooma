package uk.ac.ebi.fgpt.zooma.service;

import org.springframework.util.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationPatternDAO;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationSummaryDAO;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPattern;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 24/01/2014
 * Functional Genomics Group EMBL-EBI
 */
public class DAOBasedAnnotationPatternService implements AnnotationPatternService {

    private AnnotationPatternDAO annotationPatternDAO;

    public AnnotationPatternDAO getAnnotationPatternDAO() {
        return annotationPatternDAO;
    }

    public void setAnnotationSummaryDAO(AnnotationPatternDAO annotationPatternDAO) {
        this.annotationPatternDAO = annotationPatternDAO;
    }

    @Override
    public Collection<AnnotationPattern> readByProperty(Property property, URI... sources) {
        Collection<AnnotationPattern> patterns = new HashSet<>();
        for (AnnotationPattern pattern : getAnnotationPatternDAO().readByProperty(property)) {
            if (sources.length == 0 ) {
                patterns.add(pattern);
            }
            else if (Arrays.asList(sources).contains(pattern.getAnnotationSource().getURI())) {
                patterns.add(pattern);
            }
        }
        return patterns;
    }

    @Override
    public Collection<AnnotationPattern> search(String propertyValuePattern, URI... sources) {
        return search(null, propertyValuePattern, sources);
    }

    @Override
    public Collection<AnnotationPattern> read(URI... sources) {
        Collection<AnnotationPattern> patterns = new HashSet<>();
        for (AnnotationPattern pattern : getAnnotationPatternDAO().read()) {
            if (sources.length == 0 ) {
                patterns.add(pattern);
            }
            else if (Arrays.asList(sources).contains(pattern.getAnnotationSource().getURI())) {
                patterns.add(pattern);
            }
        }
        return patterns;
    }

    @Override
    public Collection<AnnotationPattern> read() {
        return getAnnotationPatternDAO().read();
    }

    @Override
    public Collection<AnnotationPattern> readBySemanticTag(URI semanticTagURI, URI... sources) {
        Collection<AnnotationPattern> patterns = new HashSet<>();
        for (AnnotationPattern pattern : getAnnotationPatternDAO().matchBySematicTag(semanticTagURI)) {
            if (sources.length == 0 ) {
                patterns.add(pattern);
            }
            else if (Arrays.asList(sources).contains(pattern.getAnnotationSource().getURI())) {
                patterns.add(pattern);
            }
        }
        return patterns;
    }

    @Override
    public Collection<AnnotationPattern> search(String propertyType, String propertyValuePattern, URI... sources) {

        Collection<AnnotationPattern> patterns = new HashSet<>();
        for (AnnotationPattern pattern : getAnnotationPatternDAO().matchByProperty(propertyType, propertyValuePattern)) {
            if (sources.length == 0 ) {
                patterns.add(pattern);
            }
            else if (Arrays.asList(sources).contains(pattern.getAnnotationSource().getURI())) {
                patterns.add(pattern);
            }
        }
        return patterns;
    }

}
