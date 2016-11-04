package uk.ac.ebi.spot.model;

import java.util.Collection;

/**
 * This interface defines a basic annotation consisting of a property type, property value, semantic tags and a
 * datasource name.
 *
 * @author Simon Jupp
 * @date 28/01/2014 Functional Genomics Group EMBL-EBI
 */
public interface SimpleAnnotation extends Identifiable {

    String getPropertyType();

    String getPropertyValue();

    Collection<String> getSemanticTags();

    String getSource();
}