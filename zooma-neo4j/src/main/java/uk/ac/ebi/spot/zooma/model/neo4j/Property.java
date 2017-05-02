package uk.ac.ebi.spot.zooma.model.neo4j;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Created by olgavrou on 03/08/2016.
 */
@NodeEntity(label = "Property")
@Data public class Property {

    Long id;

    private String propertyType;
    private String propertyValue;
    @Index(unique=true,primary = true)
    private String property;

    public void setPropertyType(String propertyType){
        this.propertyType = propertyType;
        if(getPropertyValue() != null){
            setProperty();
        }
    }

    public void setPropertyValue(String propertyValue){
        this.propertyValue = propertyValue;
        if(getPropertyType() != null){
            setProperty();
        }
    }

    private void setProperty(){
        this.property = propertyType+propertyValue;
    }

    @JsonIgnore
    public String getProperty(){
        return property;
    }

    @Relationship(type = "ANNOTATES")
    BiologicalEntity biologicalEntity;
}
