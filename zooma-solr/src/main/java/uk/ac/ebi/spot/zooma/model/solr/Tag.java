package uk.ac.ebi.spot.zooma.model.solr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag extends PropertyTypeValue {
    String semanticTag;
}
