package uk.ac.ebi.spot.zooma.repository.solr;

import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.solr.Recommendation;

@Repository
public interface RecommendationRepository extends SolrCrudRepository<Recommendation, String>{
}
