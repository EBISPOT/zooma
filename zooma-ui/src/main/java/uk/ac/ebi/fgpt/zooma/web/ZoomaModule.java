package uk.ac.ebi.fgpt.zooma.web;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleAbstractTypeResolver;
import org.codehaus.jackson.map.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenanceRequest;
import uk.ac.ebi.fgpt.zooma.model.AnnotationRequest;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntityRequest;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.PropertyRequest;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.model.StudyRequest;

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
        super("ZOOMA", new Version(2, 0, 0, "SNAPSHOT"));
    }

    @Override public void setupModule(SetupContext context) {
        super.setupModule(context);

        getLog().info("Customizing JSON serialization using " + getClass().getSimpleName() + ": " +
                              "registering serializers and type resolvers...");

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AnnotationProvenance.class, AnnotationProvenanceRequest.class);
        resolver.addMapping(Annotation.class, AnnotationRequest.class);
        resolver.addMapping(BiologicalEntity.class, BiologicalEntityRequest.class);
        resolver.addMapping(Property.class, PropertyRequest.class);
        resolver.addMapping(Study.class, StudyRequest.class);

        context.addAbstractTypeResolver(resolver);

        getLog().info(getClass().getSimpleName() + " setup complete!");
    }
}
