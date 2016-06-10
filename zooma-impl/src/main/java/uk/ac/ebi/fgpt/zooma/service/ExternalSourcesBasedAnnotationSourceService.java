package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;

import java.net.URI;
import java.util.Collection;

/**
 * An {@link AnnotationSourceServiceDecorator} that extends the functionality of an {@link
 * AnnotationSourceService} to support the option to add external sources and not only the ones
 * stored into the server.
 * <p>
 * This class performs the original DAO loading of sources from the {@link AnnotationSourceService} given
 * to the constructor, and then adds (or searches) the external sources
 * from the {@link AnnotationSourceService} that was set-ed
 *
 * Created by olgavrou on 09/06/2016.
 */
public class ExternalSourcesBasedAnnotationSourceService extends AnnotationSourceServiceDecorator {

    private AnnotationSourceService annotationSourceService;

    protected ExternalSourcesBasedAnnotationSourceService(AnnotationSourceService annotationSourceService) {
        super(annotationSourceService);
    }

    public AnnotationSourceService getAnnotationSourceService() {
        return annotationSourceService;
    }

    public void setAnnotationSourceService(AnnotationSourceService annotationSourceService) {
        this.annotationSourceService = annotationSourceService;
    }

    @Override
    public Collection<AnnotationSource> getAnnotationSources() {
        Collection<AnnotationSource> annotationSources = ExternalSourcesBasedAnnotationSourceService.super.getAnnotationSources();
        //add external sources
        annotationSources.addAll(getAnnotationSourceService().getAnnotationSources());
        return annotationSources;
    }

    @Override
    public AnnotationSource getAnnotationSource(String sourceName) {
        AnnotationSource annotationSource = ExternalSourcesBasedAnnotationSourceService.super.getAnnotationSource(sourceName);
        if (annotationSource == null){
            return getAnnotationSourceService().getAnnotationSource(sourceName);
        }
        return annotationSource;
    }

    @Override
    public AnnotationSource getAnnotationSource(URI uri) {
        AnnotationSource annotationSource = ExternalSourcesBasedAnnotationSourceService.super.getAnnotationSource(uri);
        if (annotationSource == null){
            return getAnnotationSourceService().getAnnotationSource(uri);
        }
        return annotationSource;
    }
}
