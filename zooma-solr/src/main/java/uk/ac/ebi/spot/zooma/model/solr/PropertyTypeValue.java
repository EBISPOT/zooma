package uk.ac.ebi.spot.zooma.model.solr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeValue {
    String propertyType;
    String propertyValue;
}
