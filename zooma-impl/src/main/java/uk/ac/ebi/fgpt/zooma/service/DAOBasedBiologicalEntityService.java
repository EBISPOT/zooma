package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.BiologicalEntityDAO;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;

/**
 * A biological entity service that uses an implementation of {@link uk.ac.ebi.fgpt.zooma.datasource.BiologicalEntityDAO}
 * to retrieve biological entities.
 *
 * @author Tony Burdett
 * @date 12/06/12
 */
public class DAOBasedBiologicalEntityService extends AbstractShortnameResolver implements BiologicalEntityService {
    private BiologicalEntityDAO biologicalEntityDAO;

    public BiologicalEntityDAO getBiologicalEntityDAO() {
        return biologicalEntityDAO;
    }

    public void setBiologicalEntityDAO(BiologicalEntityDAO biologicalEntityDAO) {
        this.biologicalEntityDAO = biologicalEntityDAO;
    }

    @Override public Collection<BiologicalEntity> getBiologicalEntities() {
        return getBiologicalEntityDAO().read();
    }

    @Override public Collection<BiologicalEntity> getBiologicalEntities(int limit, int start) {
        return getBiologicalEntityDAO().read(limit, start);
    }

    @Override public Collection<BiologicalEntity> getBiologicalEntitiesByStudy(Study study) {
        return getBiologicalEntityDAO().readByStudy(study);
    }

    @Override public BiologicalEntity getBiologicalEntity(String shortname) {
        return getBiologicalEntityDAO().read(getURIFromShortname(shortname));
    }

    @Override public BiologicalEntity getBiologicalEntity(URI uri) {
        return getBiologicalEntityDAO().read(uri);
    }
}
