package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigProd;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by olgavrou on 20/05/2016.
 */
public class OLSSearchService extends Initializable {

    private OLSClient olsClient;

    @Override
    protected void doInitialization() throws Exception {

        this.olsClient = new OLSClient(new OLSWsConfigProd());

    }

    @Override
    protected void doTermination() throws Exception {

    }

    public List<Term> getTermsByName(String value){
        return increaseScoreForDefiningOntologyTerm(getTermsByName(value, ""));
    }

    public List<Term> getTermsByName(String value, ArrayList<String> sources){

        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getTermsByName(value, source));
        }
        return increaseScoreForDefiningOntologyTerm(terms);
    }

    private List<Term> getTermsByName(String value, String source){
        return olsClient.getExactTermsByName(value, source);
    }
    //
    public List<Term> getTermsByNameFromParent(String value, String childrenOf){
        return increaseScoreForDefiningOntologyTerm(getTermsByNameFromParent(value, "", childrenOf));
    }

    public List<Term> getTermsByNameFromParent(String value, ArrayList<String> sources, String childrenOf){
        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getTermsByNameFromParent(value, source, childrenOf));
        }
        return increaseScoreForDefiningOntologyTerm(terms);
    }

    private List<Term> getTermsByNameFromParent(String value, String source, String childrenOf){
        return olsClient.getExactTermsByNameFromParent(value, source, childrenOf);
    }

    private List<Term> increaseScoreForDefiningOntologyTerm(List<Term> terms){

        for (Term term : terms){
            if (term.isDefinedOntology()){
                term.setScore(String.valueOf(Float.valueOf(term.getScore()) + 1));
            }
        }

        return terms;
    }

}
