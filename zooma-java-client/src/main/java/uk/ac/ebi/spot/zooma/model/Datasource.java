package uk.ac.ebi.spot.zooma.model;

import lombok.Value;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/01/17
 */
@Value
public class Datasource {
    String name;
    Type type;

    public enum Type {
        DATABASE,
        ONTOLOGY
    }
}
