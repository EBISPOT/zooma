package uk.ac.ebi.fgpt.zooma.model;

import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.service.Service;

import java.io.Serializable;
import java.util.Collection;

/**
 * An interface for a generic Zooma Object update. The Zooma model defines various object that have certain constraints
 * on how they can be updated. Extensions of this class must specify the update operations, this interface provides a
 * method for applying these updates to a collection of existing object.
 *
 * @author Simon Jupp
 * @date 22/01/2014 Functional Genomics Group EMBL-EBI
 */
public interface Update<T> extends Serializable {

    void apply(Collection<T> zoomaObjects, Service<T> service) throws ZoomaUpdateException;
}
