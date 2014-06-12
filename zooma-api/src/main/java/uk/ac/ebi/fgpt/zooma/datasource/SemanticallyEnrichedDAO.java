package uk.ac.ebi.fgpt.zooma.datasource;

import java.io.InputStream;

/**
 * A data access object that defines methods to acquire supplementary RDF about the ZOOMA objects that it provides
 * access to.  You would normally use this in conjunction with one of the other DAOs.  So, for example, if you had a
 * datasource that provided studies that had been typed and related to each other in an ontology, you could create an
 * class that implemented both {@link uk.ac.ebi.fgpt.zooma.datasource.StudyDAO} and this class to return the study
 * objects themselves and the typing data associated with them.
 *
 * @author Tony Burdett
 * @date 22/05/14
 */
public interface SemanticallyEnrichedDAO {
    /**
     * Retrieves an input stream containing RDF data that is supplementary to this datasource.  This RDF stream may
     * contain additional context, schema ontologies, or equivalence statements between annotations that should be
     * considered by ZOOMA and stored for reference, allowing inferences to be made over the raw annotations.
     *
     * @return an input stream containing supplementary RDF data
     */
    InputStream getSupplementaryRDFStream();
}
