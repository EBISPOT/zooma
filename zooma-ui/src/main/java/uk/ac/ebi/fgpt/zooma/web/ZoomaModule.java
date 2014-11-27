package uk.ac.ebi.fgpt.zooma.web;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.zooma.model.*;

/**
 * A Jackson module that sets up any custom JSON binding required by ZOOMA.
 *
 * @author Tony Burdett
 * @date 15/07/13
 */
@Component
public class ZoomaModule extends SimpleModule {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZoomaModule() {
        super("ZOOMA", new Version(2, 0, 0, "SNAPSHOT", "uk.ac.ebi.fgpt", "zooma-ui"));
    }

    @Override public void setupModule(SetupContext context) {
        super.setupModule(context);

        getLog().info("Customizing JSON serialization using " + getClass().getSimpleName() + ": " +
                              "registering serializers and type resolvers...");

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AnnotationProvenance.class, AnnotationProvenanceRequest.class);
        resolver.addMapping(AnnotationSource.class, AnnotationSourceRequest.class);
        resolver.addMapping(Annotation.class, AnnotationRequest.class);
        resolver.addMapping(AnnotationUpdate.class, SimpleAnnotationUpdate.class);
        resolver.addMapping(AnnotationPattern.class, SimpleAnnotationPattern.class);
        resolver.addMapping(BiologicalEntity.class, BiologicalEntityRequest.class);
        resolver.addMapping(Property.class, PropertyRequest.class);
        resolver.addMapping(Study.class, StudyRequest.class);

        context.addAbstractTypeResolver(resolver);

        getLog().info(getClass().getSimpleName() + " setup complete!");
    }
}
