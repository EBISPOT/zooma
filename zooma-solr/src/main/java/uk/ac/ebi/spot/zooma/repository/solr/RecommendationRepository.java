package uk.ac.ebi.spot.zooma.repository.solr;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.solr.Recommendation;

@Repository
@RepositoryRestController
public interface RecommendationRepository extends SolrCrudRepository<Recommendation, String>{
}
