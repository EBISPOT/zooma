package uk.ac.ebi.pride.utilities.ols.web.service.client;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigProd;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
public class OLSClientTest {

    private static OLSClient olsClient = new OLSClient(new OLSWsConfigProd());
    private static final Logger logger = LoggerFactory.getLogger(OLSClientTest.class);

    @Test
    public void testGetTermById() throws Exception {
        Term term = olsClient.getTermById(new Identifier("MS:1001767", Identifier.IdentifierType.OBO), "MS");
        Assert.assertTrue(term.getLabel().equalsIgnoreCase("nanoACQUITY UPLC System with Technology"));
    }


    @Test
    public void testGetOntologyNames() throws Exception {
        List<Ontology> ontologies = olsClient.getOntologies();
        logger.info(ontologies.toString());
        Assert.assertTrue(ontologies.size() > 0);
    }

    @Test
    public void testGetAllTermsFromOntology() throws Exception {
        List<Term> terms = olsClient.getAllTermsFromOntology("ms");
        logger.info(terms.toString());
        Assert.assertTrue(terms.size() > 0);
    }

    @Test
    public void testGetRootTerms() throws Exception {
        List<Term> rootTerms = olsClient.getRootTerms("ms");
        logger.info(rootTerms.toString());
        Assert.assertTrue(rootTerms.size() > 0);
    }

    @Test
    @Ignore
    public void testGetTermsByName() throws Exception {
        List<Term> terms = olsClient.getTermsByName("modification", "ms", false);
        logger.info(terms.toString());
        Assert.assertTrue(terms.size() > 0);
        terms = olsClient.getTermsByName("modification", "ms", true);
        Iterator iterator = terms.iterator();
        Assert.assertTrue(((Term) iterator.next()).getTermOBOId().getIdentifier().equalsIgnoreCase("MS:1001876"));
    }

    @Test
    public void testGetTermChildren() throws Exception {
        List<Term> children = olsClient.getTermChildren(new Identifier("MS:1001143", Identifier.IdentifierType.OBO), "ms", 1);
        logger.info(children.toString());
        Assert.assertTrue(contains(children, new Identifier("MS:1001568", Identifier.IdentifierType.OBO)));
    }

    private boolean contains(List<Term> terms, Identifier identifier) {
        for(Term term: terms)
           if(identifier.getType() == Identifier.IdentifierType.OBO &&
                   identifier.getIdentifier().equalsIgnoreCase(term.getTermOBOId().getIdentifier()))
               return true;
           else if(identifier.getType() == Identifier.IdentifierType.IRI &&
                   identifier.getIdentifier().equalsIgnoreCase(term.getIri().getIdentifier()))
               return true;
           else if(identifier.getType() == Identifier.IdentifierType.OWL &&
                   identifier.getIdentifier().equalsIgnoreCase(term.getShortForm().getIdentifier()))
               return true;
        return false;
    }

    @Test
    public void testIsObsolete() throws Exception {

        Boolean obsolete = olsClient.isObsolete("MS:1001057", "ms");
        Assert.assertTrue(obsolete);

    }

    @Test
    public void testGetTermsByAnnotationData() throws Exception {

        List<Term> annotations = olsClient.getTermsByAnnotationData("mod","DiffAvg", 30, 140);

        Assert.assertTrue(annotations.size() == 303);

    }

    @Test
    public void testGetTermParents() throws Exception {
        List<Term> parents = olsClient.getTermParents(new Identifier("GO:0000990", Identifier.IdentifierType.OBO), "GO", 1);
        logger.info(parents.toString());
        Assert.assertTrue(contains(parents, new Identifier("GO:0000988", Identifier.IdentifierType.OBO)));
    }

    @Test
    public void testGetExactTerm() throws Exception {
        String termLabel = "allosteric change in dynamics";
        String ontologyName = "mi";

        Term term = olsClient.getExactTermByName(termLabel, ontologyName);

        assertNotNull(term);
        assertEquals(term.getLabel(), termLabel);
        assertEquals(term.getOntologyName(), ontologyName);
        assertEquals(term.getTermOBOId().getIdentifier(), "MI:1166");
    }

    @Test
    public void testGetSynonyms() throws Exception {
        Identifier identifier = new Identifier("MI:0018", Identifier.IdentifierType.OBO);
        Set<String> synonyms = olsClient.getSynonyms(identifier, "mi");
        Assert.assertEquals(synonyms.size(), 9);
        Assert.assertTrue(synonyms.contains("classical two hybrid"));
        Assert.assertTrue(synonyms.contains("Gal4 transcription regeneration"));
        Assert.assertTrue(synonyms.contains("yeast two hybrid"));
        Assert.assertTrue(synonyms.contains("Y2H"));
        Assert.assertTrue(synonyms.contains("two-hybrid"));
        Assert.assertTrue(synonyms.contains("2 hybrid"));
        Assert.assertTrue(synonyms.contains("2-hybrid"));
        Assert.assertTrue(synonyms.contains("2h"));
        Assert.assertTrue(synonyms.contains("2H"));
    }

    @Test
    public void testGetMetaData() throws Exception {
        Identifier identifier1 = new Identifier("MI:0446", Identifier.IdentifierType.OBO);
        Map metadata1 = olsClient.getMetaData(identifier1, "mi");
        Assert.assertEquals(1, metadata1.size());
        Assert.assertNotNull(metadata1.get("definition"));
        Assert.assertEquals("PubMed is designed to provide access to citations from biomedical literature. The data can be found at both NCBI PubMed and Europe PubMed Central. \n" +
                "http://www.ncbi.nlm.nih.gov/pubmed\n" +
                "http://europepmc.org", metadata1.get("definition"));

        Identifier identifier2 = new Identifier("MOD:01161", Identifier.IdentifierType.OBO);
        Map metadata2 = olsClient.getMetaData(identifier2, "mod");
        Map synonyms = (Map) metadata2.get("synonym");
        Assert.assertEquals(2, metadata2.size());
        Assert.assertNotNull(metadata2.get("synonym"));
        Assert.assertNotNull(metadata2.get("definition"));
        Assert.assertNull(metadata2.get("comment"));
        Assert.assertEquals(3, synonyms.size());
        Assert.assertEquals("A protein modification that effectively removes oxygen atoms from a residue without the removal of hydrogen atoms.", metadata2.get("definition"));
    }

    @Test
    public void testGetTermXrefs() throws Exception {
        Identifier identifier1 = new Identifier("MI:0446", Identifier.IdentifierType.OBO);
        Map xrefs = olsClient.getTermXrefs(identifier1, "mi");
        Assert.assertEquals(3, xrefs.size());
        Assert.assertEquals("[0-9]+", xrefs.get("id-validation-regexp"));
        Assert.assertEquals("http://europepmc.org/abstract/MED/${ac}", xrefs.get("search-url"));
        Assert.assertEquals("PMID:14755292", xrefs.get("xref_definition_14755292"));
    }
}