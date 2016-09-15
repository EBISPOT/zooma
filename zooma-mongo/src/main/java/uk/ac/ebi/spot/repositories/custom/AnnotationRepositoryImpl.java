package uk.ac.ebi.spot.repositories.custom;

import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import uk.ac.ebi.spot.model.BiologicalEntity;
import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.model.Study;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the methods of the Custom repository. It uses the {@link MongoTemplate} to query and return nested fields
 * of the {@link uk.ac.ebi.spot.model.Annotation} document.
 *
 *
 * Created by olgavrou on 14/09/2016.
 */
public class AnnotationRepositoryImpl implements CustomAnnotationRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<BiologicalEntity> findDistinctAnnotatedBiologicalEntities() {
        List<DBObject> distinctValues = mongoTemplate.getCollection("annotations").distinct("annotatedBiologicalEntities");

        return convertListOfDBObjectToListOfBiologicalEntity(distinctValues);
    }

    @Override
    public List<BiologicalEntity> findDistinctAnnotatedBiologicalEntitiesByAnnotatedBiologicalEntitiesStudiesAccession(String accession) {
        Query query = new Query();
        query.addCriteria(Criteria.where("annotatedBiologicalEntities.studies.accession").is(accession));
        List<DBObject> distinctEntities = mongoTemplate.getCollection("annotations").distinct("annotatedBiologicalEntities", query.getQueryObject());

        return convertListOfDBObjectToListOfBiologicalEntity(distinctEntities);
    }

    @Override
    public List<Study> findDistinctAnnotatedBiologicalEntitiesStudies() {

        List<DBObject> distinctValues = mongoTemplate.getCollection("annotations").distinct("annotatedBiologicalEntities.studies");
        return convertListOfDBObjectToListOfStudy(distinctValues);
    }

    @Override
    public List<Study> findDistinctAnnotatedBiologicalEntitiesStudiesBySemanticTags(URI... semanticTags) {
        //uris are saved as strings in mongodb, so need to search as <fieldname>.string
        List<String> semanticTagsToStrings = new ArrayList<>();
        for (URI semanticTag : semanticTags){
            semanticTagsToStrings.add(semanticTag.toString());
        }

        Query query = new Query();

        List<Criteria> criterias = new ArrayList<>();
        for(String semanticTag : semanticTagsToStrings){
            criterias.add(Criteria.where("semanticTags.string").is(semanticTag));
        }
        Criteria criteria = Criteria.where("");
        criteria = criteria.andOperator(criterias.toArray(new Criteria[criterias.size()]));

        query.addCriteria(criteria);

        List<DBObject> retrievedStudies = mongoTemplate.getCollection("annotations").distinct("annotatedBiologicalEntities.studies", query.getQueryObject());

        return convertListOfDBObjectToListOfStudy(retrievedStudies);
    }

    @Override
    public List<Study> findDistinctAnnotatedBiologicalEntitiesStudiesByAccession(String accession) {

        Query query = new Query();
        query.addCriteria(Criteria.where("annotatedBiologicalEntities.studies.accession").is(accession));
        List<DBObject> retrievedStudies = mongoTemplate.getCollection("annotations").distinct("annotatedBiologicalEntities.studies", query.getQueryObject());

        //the way this query works is to find the annotation with the study accession queried for, and then retrieve the studies that the annotation has
        //we therefore need to remove the potential other studies that came with this annotation
        List<Study> studies = convertListOfDBObjectToListOfStudy(retrievedStudies);
        List<Study> finalStudies = new ArrayList<>();
        for (Study study : studies){
            if (study.getAccession().equals(accession)){
                finalStudies.add(study);
            }
        }
        return finalStudies;
    }

    @Override
    public List<Property> findDistinctAnnotatedProperties() {
        List<DBObject> distinctValues = mongoTemplate.getCollection("annotations").distinct("annotatedProperty");

        return convertListOfDBObjectToListOfProperty(distinctValues);
    }

    @Override
    public List<String> findAllPropertyTypes() {
        return mongoTemplate.getCollection("annotations").distinct("annotatedProperty.propertyType");
    }

    @Override
    public List<Property> findAnnotatedPropertyByAnnotatedPropertyPropertyType(String type) {
        Query query = new Query();
        query.addCriteria(Criteria.where("annotatedProperty.propertyType").is(type));
        List<DBObject> retrievedProperties = mongoTemplate.getCollection("annotations").distinct("annotatedProperty", query.getQueryObject());

        return convertListOfDBObjectToListOfProperty(retrievedProperties);
    }

    @Override
    public List<Property> findAnnotatedPropertyByAnnotatedPropertyPropertyValue(String value) {
        //no type
        Query query = new Query();
        query.addCriteria(Criteria.where("annotatedProperty.propertyValue").is(value).andOperator(Criteria.where("annotatedProperty.propertyType").exists(false)));
        List<DBObject> retrievedProperties = mongoTemplate.getCollection("annotations").distinct("annotatedProperty", query.getQueryObject());

        return convertListOfDBObjectToListOfProperty(retrievedProperties);
    }

    @Override
    public List<Property> findAnnotatedPropertyByAnnotatedPropertyPropertyTypeAndByAnnotatedPropertyPropertyValue(String type, String value) {
        //if type is null but value is not null, search by value
        //if value is null but type is not null, search by type
        Query query = new Query();
        if (type == null || type.equals("")){
            if(value != null && !value.equals("")){
                query.addCriteria(Criteria.where("annotatedProperty.propertyValue").is(value));
            } else {
                return new ArrayList<>();
            }

        } else if (value == null || value.equals("")){
            if (type != null && !type.equals("")){
                query.addCriteria(Criteria.where("annotatedProperty.propertyType").is(type));
            } else {
                return new ArrayList<>();
            }
        } else {
            query.addCriteria(Criteria.where("annotatedProperty.propertyValue").is(value).andOperator(Criteria.where("annotatedProperty.propertyType").is(type)));
        }

        List<DBObject> retrievedProperties = mongoTemplate.getCollection("annotations").distinct("annotatedProperty", query.getQueryObject());
        return convertListOfDBObjectToListOfProperty(retrievedProperties);
    }


    private List<Study> convertListOfDBObjectToListOfStudy(List<DBObject> retrievedStudies){
        List<Study> studies = new ArrayList<>();
        for (DBObject study : retrievedStudies){
            studies.add(mongoTemplate.getConverter().read(Study.class, study));
        }
        return studies;
    }

    private List<BiologicalEntity> convertListOfDBObjectToListOfBiologicalEntity(List<DBObject> retrievedEntities){
        List<BiologicalEntity> biologicalEntities = new ArrayList<>();
        for (DBObject entity : retrievedEntities){
            biologicalEntities.add(mongoTemplate.getConverter().read(BiologicalEntity.class, entity));
        }
        return biologicalEntities;
    }

    private List<Property> convertListOfDBObjectToListOfProperty(List<DBObject> retrievedProperties){
        List<Property> properties = new ArrayList<>();
        for(DBObject property : retrievedProperties){
            properties.add(mongoTemplate.getConverter().read(Property.class, property));
        }
        return properties;
    }

}
