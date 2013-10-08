package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;

import java.util.Collection;

/**
 * A property type service that uses an implementation of {@link uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO} to
 * retrieve property instances and extract the range of types.
 *
 * @author Tony Burdett
 * @date 03/04/12
 */
public class DAOBasedPropertyTypeService extends AbstractShortnameResolver implements PropertyTypeService {
    private PropertyDAO propertyDAO;

    public PropertyDAO getPropertyDAO() {
        return propertyDAO;
    }

    public void setPropertyDAO(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    @Override public Collection<String> getPropertyTypes() {
        return getPropertyDAO().readTypes();
    }

    @Override public Collection<String> getPropertyTypes(int limit, int start) {
        return getPropertyDAO().readTypes(limit, start);
    }
}
