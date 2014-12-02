package uk.ac.ebi.fgpt.zooma.datasource;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import uk.ac.ebi.fgpt.zooma.util.OntologyAccessionUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 04/10/12
 */
public class AtlasAnnotationMapper extends DefaultJdbcAnnotationMapper {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public AtlasAnnotationMapper(AtlasAnnotationFactory atlasAnnotationFactory) {
        this(atlasAnnotationFactory, "http://www.ebi.ac.uk/efo/efo.owl");
    }

    public AtlasAnnotationMapper(AtlasAnnotationFactory atlasAnnotationFactory, Resource efoResource) {
        super(atlasAnnotationFactory);
        try {
            OntologyAccessionUtils.loadOntology(efoResource.getURL());
        }
        catch (IOException e) {
            throw new RuntimeException("Unexpected error loading EFO from resource " + efoResource.toString(), e);
        }
    }

    public AtlasAnnotationMapper(AtlasAnnotationFactory atlasAnnotationFactory, String efoURL) {
        super(atlasAnnotationFactory);
        try {
            URL efo = URI.create(efoURL).toURL();
            OntologyAccessionUtils.loadOntology(efo);
        }
        catch (IOException e) {
            throw new RuntimeException("Unexpected error forming EFO URL", e);
        }
    }

    @Override protected URI convertSemanticTagToURI(String semanticTag) {
        if (semanticTag == null) {
            getLog().debug("Received null semantic tag result from database query");
            return null;
        }
        else {
            IRI iri = OntologyAccessionUtils.getIRIFromAccession(semanticTag);
            if (iri != null) {
                return iri.toURI();
            }
            else {
                getLog().trace("Unable to lookup semantic tag for '" + semanticTag + "', leaving as null");
                return null;
            }
        }
    }
}
