package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.Study;
import uk.ac.ebi.spot.repositories.StudyRepository;

import java.net.URI;
import java.util.List;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Service
public class StudyRepositoryService  {

    @Autowired
    StudyRepository studyRepository;

    public List<Study> getDistinctStudies(){
        return studyRepository.findDistinctStudies();
    }

    public List<Study> getBySemanticTags(URI... semanticTags){
        return studyRepository.findBySemanticTags(semanticTags);
    }

    public List<Study> getByAccession(String accession){
        return studyRepository.findByAccession(accession);
    }
}
