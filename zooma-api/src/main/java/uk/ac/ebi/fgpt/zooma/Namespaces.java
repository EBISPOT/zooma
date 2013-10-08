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
    ANNOTATION("http://rdf.ebi.ac.uk/resource/zooma/annotation/"),
    PROPVAL("http://rdf.ebi.ac.uk/resource/zooma/propertyvalue/"),

    EBI("http://www.ebi.ac.uk/"),
    EBIRESOURCE("http://rdf.ebi.ac.uk/resource/"),

    ARRAYEXPRESS("http://www.ebi.ac.uk/arrayexpress/"),
    AE_RESOURCE("http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/"),
    AE_ANNOTATION("http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/annotation/"),
    AE_EXPERIMENT("http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/experiment/"),
    AE_PROPERTY("http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/property/"),

    GXA("http://www.ebi.ac.uk/gxa/"),
    GXA_RESOURCE("http://rdf.ebi.ac.uk/resource/zooma/gxa/"),
    GXA_ANNOTATION("http://rdf.ebi.ac.uk/resource/zooma/gxa/annotation/"),
    GXA_EXPERIMENT("http://rdf.ebi.ac.uk/resource/zooma/gxa/experiment/"),
    GXA_PROPERTY("http://rdf.ebi.ac.uk/resource/zooma/gxa/property/"),

    GENOME("http://www.genome.gov/"),
    GWAS("http://www.genome.gov/gwastudies/"),
    GWAS_RESOURCE("http://rdf.ebi.ac.uk/resource/zooma/gwas/"),
    GWAS_ANNOTATION("http://rdf.ebi.ac.uk/resource/zooma/gwas/annotation/"),
    GWAS_SNP("http://rdf.ebi.ac.uk/resource/zooma/gwas/snp/"),
    GWAS_PROPERTY("http://rdf.ebi.ac.uk/resource/zooma/gwas/property/"),

    OWL_RESOURCE("http://rdf.ebi.ac.uk/resource/zooma/owl/"),
    OWL_ANNOTATION("http://rdf.ebi.ac.uk/resource/zooma/owl/annotation/"),
    OWL_PROPERTY("http://rdf.ebi.ac.uk/resource/zooma/owl/property/"),

    OMIA("http://omia.angis.org.au/"),
    OMIA_RESOURCE("http://rdf.ebi.ac.uk/resource/zooma/omia/"),
    OMIA_ANNOTATION("http://rdf.ebi.ac.uk/resource/zooma/omia/annotation/"),
    OMIA_PROPERTY("http://rdf.ebi.ac.uk/resource/zooma/omia/property/"),

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
    ORPHA("http://www.orphanet.org/rdfns#");

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
