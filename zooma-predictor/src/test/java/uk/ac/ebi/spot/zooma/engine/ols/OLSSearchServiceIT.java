package uk.ac.ebi.spot.zooma.engine.ols;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by olgavrou on 05/06/2017.
 */
public class OLSSearchServiceIT {

    private OLSClient olsClient = new OLSClient(new OLSWsConfig());
    private OLSSearchService olsSearchService = new OLSSearchService(olsClient);

    @Test
    public void getTermsByName() throws Exception {
        String label = "liver";
        List<Term> terms = olsSearchService.getTermsByName(label);
        assertTrue(terms.size() > 0);
        for(Term term : terms){
            assertTrue(term.getLabel().toLowerCase().equals(label));
        }
    }

    @Test
    public void getTermsByNameWithSources() throws Exception {
        String label = "liver";
        List<String> sources = new ArrayList<>();
        sources.add("efo");
        sources.add("uberon");

        List<Term> terms = olsSearchService.getTermsByName(label, sources);
        assertTrue(terms.size() > 0);
        for(Term term : terms){
            assertTrue(term.getLabel().toLowerCase().equals(label));
            assertTrue(term.getOntologyIri().equals("efo") || term.getOntologyName().equals("uberon"));
        }
    }

    @Test
    public void getTermsByNameFromParent() throws Exception {
        //ATTENTION: could fail if parent changes in ontology
        String label = "liver";
        String parent = "http://purl.obolibrary.org/obo/UBERON_0006925";

        List<Term> terms = olsSearchService.getTermsByNameFromParent(label, parent);
        assertTrue(terms.size() > 0);
        for(Term term : terms){
            assertTrue(term.getLabel().toLowerCase().equals(label));
            assertTrue(term.getOntologyIri().equals("efo") || term.getOntologyName().equals("uberon"));
            List<Term> parents = olsClient.getTermParents(term.getIri(), "uberon", 1);
            boolean found = false;
            for(Term parentTerm : parents){
                if(parentTerm.getIri().getIdentifier().equals(parent)){
                    found = true;
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void getTermsByNameFromParentWithSources() throws Exception {
        //ATTENTION: could fail if parent changes in ontology
        String label = "liver";
        String parent = "http://purl.obolibrary.org/obo/UBERON_0006925";
        List<String> sources = new ArrayList<>();
        sources.add("efo");
        sources.add("uberon");

        List<Term> terms = olsSearchService.getTermsByNameFromParent(label, sources, parent);
        assertTrue(terms.size() > 0);
        for(Term term : terms){
            assertTrue(term.getLabel().toLowerCase().equals(label));
            List<Term> parents = olsClient.getTermParents(term.getIri(), "uberon", 1);
            boolean found = false;
            for(Term parentTerm : parents){
                if(parentTerm.getIri().getIdentifier().equals(parent)){
                    found = true;
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void getLabelByIri() throws Exception {
        //ATTENTION: could fail if iri becomes obsolete
        String label = "liver";
        String iri = "http://purl.obolibrary.org/obo/UBERON_0002107";

        String termLabel = olsSearchService.getLabelByIri(iri);
        assertTrue(termLabel.toLowerCase().equals(label));

    }

    @Test
    public void isReplaceable() throws Exception {
        //ATTENTION: could break if term gets deleted or it's replacement is removed
        boolean replaceable = olsSearchService.isReplaceable(new URI("http://www.ebi.ac.uk/efo/EFO_0000891"));
        assertTrue(replaceable);
    }

    @Test
    public void replaceSemanticTag() throws Exception {

    }

    @Test
    public void getAllOntologies() throws Exception {

    }

    @Test
    public void getOntology() throws Exception {

    }

    @Test
    public void getOntologyNamespaceFromId() throws Exception {

    }

}