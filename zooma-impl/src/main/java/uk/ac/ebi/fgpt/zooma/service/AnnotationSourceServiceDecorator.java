package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;

import java.net.URI;
import java.util.Collection;

/**
 * An abstract decorator of a {@link AnnotationSourceService}.  You should subclass this decorator to create different
 * decorations that add functionality to collecting sources.
 *
 * Created by olgavrou on 09/06/2016.
 */
public abstract class AnnotationSourceServiceDecorator implements AnnotationSourceService{

    private final AnnotationSourceService _annotationSourceService;

    protected AnnotationSourceServiceDecorator(AnnotationSourceService annotationSourceService) {
        this._annotationSourceService = annotationSourceService;
    }

    @Override
    public Collection<AnnotationSource> getAnnotationSources() {
        return _annotationSourceService.getAnnotationSources();
    }

    @Override
    public AnnotationSource getAnnotationSource(String sourceName) {
        return _annotationSourceService.getAnnotationSource(sourceName);
    }

    @Override
    public AnnotationSource getAnnotationSource(URI uri) {
        return _annotationSourceService.getAnnotationSource(uri);
    }
}
