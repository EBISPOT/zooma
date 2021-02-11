

import React, { Component, Fragment } from 'react'

import { Row, Column } from 'react-foundation'



import { EBIFooter, EBIMasthead } from '../components/EBI'
import ReactLode from '../components/ReactLode'
import ZoomaNav from '../Navbar'


interface Props {
}

interface State {
}

export default class Sparql extends Component<Props, State> {

    constructor(props) {
      super(props)

      this.state = {
      }
    }

    componentDidMount() {

    }

    render() {

        return (
            <main>
              <ReactLode namespacePrefixes={namespacePrefixes} exampleQueries={exampleQueries} defaultQuery={defaultQuery} servletBase="/spot/zooma/v2/api" />
            </main>
        )
    }

}


let namespacePrefixes = new Map(Object.entries({
  rdf: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
  rdfs: 'http://www.w3.org/2000/01/rdf-schema#',
  owl: 'http://www.w3.org/2002/07/owl#',
  dc: 'http://purl.org/dc/elements/1.1/',
  obo: 'http://purl.obolibrary.org/obo/',
  efo: 'http://www.ebi.ac.uk/efo/',
  zoomaresource: 'http://rdf.ebi.ac.uk/resource/zooma/',
  zoomaterms: 'http://rdf.ebi.ac.uk/terms/zooma/',
  oac: 'http://www.openannotation.org/ns/'
}))

let defaultQuery = `SELECT DISTINCT ?annotation WHERE {
    ?annotation a oac:DataAnnotation 
}
`

let exampleQueries = [
  {
    shortname: "Query 1",
    description: "Get properties annotated to EFO term 'Ovarian cancer' (EFO_0001075)",
    query: "SELECT DISTINCT ?propertyname ?propertyvalue WHERE {\n\n" +
      "?annotationid rdf:type oac:DataAnnotation ;\n" +
      "\t\t oac:hasBody ?propertyvalueid ;\n" +
      "\t\t oac:hasBody efo:EFO_0001075 .\n" +
      "?propertyvalueid zoomaterms:propertyName ?propertyname ;\n" +
      "\t\t zoomaterms:propertyValue ?propertyvalue .\n" +
      "}"
  },
  {
    shortname: "Query 2",
    description: "Get ontology annotations from all databases for property 'organism part'/liver",
    query: "SELECT DISTINCT ?propertyvalueid ?propertyname ?propertyvalue ?ontologyTerm ?ontologyTermLabel WHERE {\n\n" +
      "?propertyvalueid rdf:type zoomaterms:Property ;\n" +
      "\t\t zoomaterms:propertyName ?propertyname ;\n" +
      "\t\t  zoomaterms:propertyValue ?propertyvalue . \n\n" +
      "filter regex (?propertyname, \"organism part\", \"i\") .\n" +
      "filter regex (?propertyvalue, \"liver\", \"i\") . \n\n" +
      "?annotationid oac:hasBody ?propertyvalueid ;  \n" +
      "\t\t oac:hasBody ?ontologyTerm ;\n" +
      "\t\t  rdf:type oac:DataAnnotation .  \n" +
      "?ontologyTerm rdf:type oac:SemanticTag .    \n" +
      "OPTIONAL {?ontologyTerm rdfs:label ?ontologyTermLabel } .\n" +
      "}"
  },
  {
    shortname: "Query 3",
    description: "Get all annotations for ArrayExpress experiment E-GEOD-13763",
    query: "SELECT DISTINCT ?study ?assay ?databaseid ?propertyname ?propertyvalue ?semantictag ?evidence  WHERE {\n\n" +
      "?annotationid rdf:type oac:DataAnnotation ;\n" +
      "\t\t oac:hasBody ?propertyvalueid ;\n" +
      "\t\t  oac:hasBody ?semantictag . \n" +
      "\t\t ?semantictag rdf:type oac:SemanticTag . \n\n" +

      "?propertyvalueid zoomaterms:propertyName ?propertyname ;\n" +
      "\t\t zoomaterms:propertyValue ?propertyvalue .\n\n" +
      "?annotationid oac:hasTarget ?assay .\n" +
      "?assay dc:isPartOf <http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/E-GEOD-13763> .\n" +
      "?assay dc:isPartOf ?study .\n\n" +
      "OPTIONAL {?annotationid dc:source ?databaseid} .   \n" +
      "OPTIONAL {?annotationid zoomaterms:hasEvidence ?evidence} . \n" +
      "}"
  },
  {
    shortname: "Query 4",
    description: "Get all experiments where the samples is some brain part and the disease is a nervous system disease",
    query: "SELECT DISTINCT ?study ?assayName ?brainTermLabel ?diseaseTermLabel WHERE {\n\n" +

      "?assay dc:isPartOf ?study .\n" +
      "?assay rdfs:label ?assayName .  \n" +
      "?annotation1 oac:hasTarget ?assay . \n" +
      "?annotation2 oac:hasTarget ?assay . \n\n" +

      "?annotation1 oac:hasBody ?propertyvalueid1 .   \n" +
      "?propertyvalueid1 zoomaterms:propertyName ?propertyname1 .\n" +
      "?propertyvalueid1 zoomaterms:propertyValue ?propertyvalue1 . \n" +
      "?annotation1 oac:hasBody ?tag1  . \n" +
      "?tag1 rdfs:label ?brainTermLabel . \n" +
      "?tag1 rdfs:subClassOf obo:UBERON_0000955 .  \n\n" +

      "?annotation2 oac:hasBody ?propertyvalueid2 .\n" +
      "?propertyvalueid2 zoomaterms:propertyName ?propertyname2 .\n" +
      "?propertyvalueid2 zoomaterms:propertyValue ?propertyvalue2 . \n" +
      "?annotation2 oac:hasBody ?tag2  .\n" +
      "?tag2 rdfs:label ?diseaseTermLabel . \n" +
      "?tag2 rdfs:subClassOf efo:EFO_0000618 .  \n" +
      "}"

  },
  {
    shortname: "Query 5",
    description: "Get all annotations",
    query: "SELECT DISTINCT ?study ?bioentityid  ?databaseid ?evidence ?propertyname ?propertyvalue ?semantictag ?generator ?generated WHERE {\n\n" +

      "?annotationid rdf:type oac:DataAnnotation .\n" +
      "?annotationid oac:hasBody ?propertyvalueid .\n" +
      "?propertyvalueid zoomaterms:propertyName ?propertyname .\n" +
      "?propertyvalueid zoomaterms:propertyValue ?propertyvalue .\n" +

      "OPTIONAL {\n\t?annotationid oac:hasTarget ?bioentityid .\n" +
      "\t?bioentityid dc:isPartOf ?study} .\n" +
      "OPTIONAL {\n" +
      "\t?annotationid oac:hasBody ?semantictag .\n" +
      "\t?semantictag rdf:type oac:SemanticTag \n" +
      " } .\n" +
      "OPTIONAL {?annotationid dc:source ?databaseid} .\n" +
      "OPTIONAL {?annotationid zoomaterms:hasEvidence ?evidence} .\n" +
      "OPTIONAL {?annotationid oac:generator ?generator} .\n" +
      "OPTIONAL {?annotationid oac:generated ?generated} .\n" +
      "}\n"
  }

]