package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.BiologicalEntity;
import uk.ac.ebi.spot.repositories.BiologicalEntityRepository;

import java.util.List;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Service
public class BiologicalEntityRepositoryService  {

    @Autowired
    BiologicalEntityRepository biologicalEntityRepository;

    public List<BiologicalEntity> getDistinctByStudyAccession(String accession){
        return biologicalEntityRepository.findDistinctByStudiesAccession(accession);
    }

    public List<BiologicalEntity> getDistinctBiologicalEntities(){
        return biologicalEntityRepository.findDistinctAnnotatedBiologicalEntities();
    }

}
