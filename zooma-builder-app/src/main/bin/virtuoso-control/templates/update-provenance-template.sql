SPARQL INSERT INTO <http://rdf.ebi.ac.uk/dataset/zooma/description> {
<http://www.ebi.ac.uk/fgpt/zooma/sparql>     a       <http://www.w3.org/ns/sparql-service-description#Service> ;
     <http://www.w3.org/ns/sparql-service-description#defaultEntailmentRegime>
             <http://www.w3.org/ns/entailment/RDFS> ;
     <http://www.w3.org/ns/sparql-service-description#endpoint>
             <http://www.ebi.ac.uk/fgpt/zooma/sparql> ;
     <http://www.w3.org/ns/sparql-service-description#feature>
             <http://www.w3.org/ns/sparql-service-description#BasicFederatedQuery> ;
     <http://www.w3.org/ns/sparql-service-description#resultFormat>
             <http://www.w3.org/ns/formats/SPARQL_Results_JSON> , <http://www.w3.org/ns/formats/RDF_XML> , <http://www.w3.org/ns/formats/N3> , <http://www.w3.org/ns/formats/SPARQL_Results_XML> ;
     <http://www.w3.org/ns/sparql-service-description#supportedLanguage>
             <http://www.w3.org/ns/sparql-service-description#SPARQL11Query> .
};
SPARQL INSERT INTO <http://rdf.ebi.ac.uk/dataset/zooma/description> {
<http://rdf.ebi.ac.uk/dataset/zooma> <http://rdfs.org/ns/void#triples> ?count
}
WHERE {
 {
  select count(*) as ?count where {
   GRAPH <http://rdf.ebi.ac.uk/dataset/zooma> {
     ?s ?p ?o
    }
  }
 }
};