package uk.ac.ebi.fgpt.zooma;

import java.net.URI;

/**
 * A list of namespaces known to ZOOMA
 *
 * @author Simon Jupp
 * @date 03/04/2012 Functional Genomics Group EMBL-EBI
 */
public enum Namespaces {
    OWL("http://www.w3.org/2002/07/owl#"),
    RDFS("http://www.w3.org/2000/01/rdf-schema#"),
    RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    XSD("http://www.w3.org/2001/XMLSchema#"),
    XML("http://www.w3.org/XML/1998/namespace"),
    SWRL("http://www.w3.org/2003/11/swrl#"),
    SWRLB("http://www.w3.org/2003/11/swrlb#"),
    SKOS("http://www.w3.org/2004/02/skos/core#"),
    DC("http://purl.org/dc/elements/1.1/"),

    OAC("http://www.openannotation.org/ns/"),
    PROV("http://www.w3.org/TR/prov-o/"),

    ZOOMA("http://www.ebi.ac.uk/fgpt/zooma/"),
    ZOOMA_TERMS("http://rdf.ebi.ac.uk/terms/zooma/"),
    ZOOMA_RESOURCE("http://rdf.ebi.ac.uk/resource/zooma/"),

    EBI("http://www.ebi.ac.uk/"),
    EBIRESOURCE("http://rdf.ebi.ac.uk/resource/"),

    ARRAYEXPRESS("http://www.ebi.ac.uk/arrayexpress"),

    ATLAS("http://www.ebi.ac.uk/gxa"),
    GXA("http://www-test.ebi.ac.uk/gxa"),

    GENOME("http://www.genome.gov"),
    GWAS("http://www.genome.gov/gwastudies"),

    CHEMBL("http://www.ebi.ac.uk/chembl"),

    OWL_RESOURCE("http://rdf.ebi.ac.uk/resource/zooma/owl/"),

    OMIA("http://omia.angis.org.au"),
    OMIM("http://omim.org"),

    UNIPROT("http://www.uniprot.org"),

    EFO("http://www.ebi.ac.uk/efo/"),
    SNAP("http://www.ifomis.org/bfo/1.1/snap#"),
    SPAN("http://www.ifomis.org/bfo/1.1/span#"),
    CL("http://purl.org/obo/owl/CL#"),
    OBO("http://purl.obolibrary.org/obo/"),
    OBOINOWL("http://www.geneontology.org/formats/oboInOwl#"),
    NCBITAXON("http://purl.org/obo/owl/NCBITaxon#"),
    BTO("http://purl.org/obo/owl/BTO#"),
    PATO("http://purl.org/obo/owl/PATO#"),
    PUBMED("http://europepmc.org/abstract/MED/"),
    ORPHAEFO("http://www.orphanet.org/rdfns#");

    private String ns;

    private Namespaces(String ns) {
        this.ns = ns;
    }

    public URI getURI() {
        return URI.create(ns);
    }

/*
  The following is the list of namespaces configured in the file zooma/prefix.properties in the zooma-ui module and the
  zooma-cli module.

  We should find a better way to represent these namespaces such that they are not defined twice.

  The above enum should ideally be in sync with this list

  -------------------------------------------------------------------------------------------------
  owl            http://www.w3.org/2002/07/owl#
  rdfs           http://www.w3.org/2000/01/rdf-schema#
  rdf            http://www.w3.org/1999/02/22-rdf-syntax-ns#
  xsd            http://www.w3.org/2001/XMLSchema#
  xml            http://www.w3.org/XML/1998/namespace
  swrl           http://www.w3.org/2003/11/swrl#
  swrlb          http://www.w3.org/2003/11/swrlb#
  skos           http://www.w3.org/2004/02/skos/core#
  dc             http://purl.org/dc/elements/1.1/

  oac            http://www.openannotation.org/ns/
  prov           http://www.w3.org/TR/prov-o/

  zooma          http://www.ebi.ac.uk/fgpt/zooma/
  zoomaterms     http://rdf.ebi.ac.uk/terms/zooma/
  zoomaresource  http://rdf.ebi.ac.uk/resource/zooma/
  annotation     http://rdf.ebi.ac.uk/resource/zooma/annotation/
  propval        http://rdf.ebi.ac.uk/resource/zooma/propertyvalue/

  ebi            http://www.ebi.ac.uk/
  ebiresource    http://rdf.ebi.ac.uk/resource/

  arrayexpress   http://www.ebi.ac.uk/arrayexpress/
  aeresource     http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/
  aeannotation   http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/annotation/
  aeexperiment   http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/experiment/
  aeproperty     http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/property/

  gxa            http://www.ebi.ac.uk/gxa/
  gxaresource    http://rdf.ebi.ac.uk/resource/zooma/gxa/
  gxaannotation  http://rdf.ebi.ac.uk/resource/zooma/gxa/annotation/
  gxaexperiment  http://rdf.ebi.ac.uk/resource/zooma/gxa/experiment/
  gxaproperty    http://rdf.ebi.ac.uk/resource/zooma/gxa/property/

  genome         http://www.genome.gov/
  gwas           http://www.genome.gov/gwastudies/
  gwasresource   http://rdf.ebi.ac.uk/resource/zooma/gwas/
  gwasannotation http://rdf.ebi.ac.uk/resource/zooma/gwas/annotation/
  gwassnp        http://rdf.ebi.ac.uk/resource/zooma/gwas/snp/
  gwasproperty   http://rdf.ebi.ac.uk/resource/zooma/gwas/property/

  owlresource    http://rdf.ebi.ac.uk/resource/zooma/owl/
  owlannotation  http://rdf.ebi.ac.uk/resource/zooma/owl/annotation/
  owlproperty    http://rdf.ebi.ac.uk/resource/zooma/owl/property/

  efo            http://www.ebi.ac.uk/efo/
  snap           http://www.ifomis.org/bfo/1.1/snap#
  span           http://www.ifomis.org/bfo/1.1/span#
  cl             http://purl.org/obo/owl/CL#
  obo            http://purl.obolibrary.org/obo/
  oboinowl       http://www.geneontology.org/formats/oboInOwl#
  ncbitaxon      http://purl.org/obo/owl/NCBITaxon#
  bto            http://purl.org/obo/owl/BTO#
  pato           http://purl.org/obo/owl/PATO#
  pubmed         http://europepmc.org/abstract/MED/
  orpha          http://www.orphanet.org/rdfns#
  -------------------------------------------------------------------------------------------------
*/
}
