import { Link } from 'react-router-dom'
import React, { Fragment } from "react";
import { Row } from "react-foundation";
import { EBIMasthead, EBIFooter } from "../../components/EBI";
import ZoomaNav from "../../Navbar";

export default function developerDocs() {
    return (
            <main>
              <section>
                <h3>ZOOMA Annotation Model</h3>
                <p>
                  The ZOOMA annotation model describes annotations between a property - plain text "property values", optionally
                  constrained by a type - and a semantic tag, in the context of a biological entity. It also represents the
                  provenance of the asserted annotation.
                </p>
                <p>
                  The general annotation model for ZOOMA is shown below.<br /> <img src={process.env.PUBLIC_URL + "/images/zooma_annotation_model.gif"} alt="ZOOMA annotation model" style={{display: 'block', margin: 'auto'}} />
                </p>
                <p>
                  Below is a worked example of a real annotation using the ZOOMA
                  model.<br /><img src={process.env.PUBLIC_URL + "/images/zooma_annotation_example.gif"} alt="ZOOMA annotation example" style={{display: 'block', margin: 'auto'}} />
                </p>
              </section>
              <section>
                <h3>Evidence Codes</h3>
                <p>
                  The ZOOMA evidence codes, ranked in order of preference, are described below.
                </p>
                <ol>
                  <li>
                    <span className="docs-label">MANUAL_CURATED</span> - http://purl.obolibrary.org/obo/ECO_0000305
                    <ul>
                      <li>A type of curator inference that is used in a manual assertion.</li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">ZOOMA_INFERRED_FROM_CURATED</span> -
                    http://rdf.ebi.ac.uk/terms/zooma/ZOOMA_0000101
                    <ul>
                      <li>An annotation inferred by ZOOMA from previous curated entry. A type of Automatic curation.
                      </li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">AUTOMATIC</span> - http://purl.obolibrary.org/obo/ECO_0000203
                    <ul>
                      <li>An assertion method that does not involve human review.</li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">COMPUTED_FROM_ONTOLOGY</span> - http://rdf.ebi.ac.uk/terms/zooma/ZOOMA_0000102
                    <ul>
                      <li>An evidence code that states the existence of an annotation was computed based on a match to a
                        semantic tag. Use this when a property value exactly matches a class label or synonym from an
                        ontology.
                      </li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">COMPUTED_FROM_TEXT_MATCH</span> -
                    http://rdf.ebi.ac.uk/terms/zooma/ZOOMA_0000103
                    <ul>
                      <li>An evidence code that states the existence of an annotation was computed based on a text match to a
                        previous annotation that has not been curated. This is a computed match based on a match that is (at
                        best) inferred to exist, and hence has at least two degrees of separation from a curated match. This
                        is therefore assigned a low confidence.
                      </li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">SUBMITTER_PROVIDED</span> - http://rdf.ebi.ac.uk/terms/zooma/ZOOMA_0000104
                    <ul>
                      <li>An evidence code that states the existence of an annotation was provided by a submitter, usually
                        within the scope of a single Study or Biological Entity. This annotation has never subsequently been
                        confirmed or curated and may not represent a good annotation with respect to other available
                        annotations.
                      </li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">NON_TRACEABLE</span> - http://rdf.ebi.ac.uk/terms/zooma/ZOOMA_0000105
                    <ul>
                      <li>An evidence code that states this annotation was supported by evidence at some point, but it is
                        unknown what this evidence was, or that the evidence for this annotation has been lost.
                      </li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">NO_EVIDENCE</span> - http://rdf.ebi.ac.uk/terms/zooma/ZOOMA_0000106
                    <ul>
                      <li>An evidence code that states this annotation has no evidence to support it.</li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">UNKNOWN</span> - http://rdf.ebi.ac.uk/terms/zooma/ZOOMA_0000107
                    <ul>
                      <li>An evidence code that indicates it is unknown if this annotation has ever had evidence to support
                        it.
                      </li>
                    </ul>
                  </li>
                </ol>
                <p />
              </section>
              <section>
                <h3>Annotation Provenance</h3>
                <p>
                  The following information is recorded as part of the ZOOMA annotation provenance model:
                </p>
                <ul>
                  <li>
                    <span className="docs-label">SOURCE</span>
                    <ul>
                      <li>the URI of the datasource</li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">EVIDENCE</span>
                    <ul>
                      <li>One of the evidence codes, above</li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">GENERATOR</span>
                    <ul>
                      <li>The agent that generated the annotation object</li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">GENERATION DATE</span>
                    <ul>
                      <li>The date on which the annotation was generated in the ZOOMA knowledgebase</li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">ANNOTATOR</span>
                    <ul>
                      <li>The agent who generated the annotation. This is usually a person name</li>
                    </ul>
                  </li>
                  <li>
                    <span className="docs-label">ANNOTATION DATE</span>
                    <ul>
                      <li>The date on which the annotation was generated in the source database</li>
                    </ul>
                  </li>
                </ul>
              </section>
              <section>
                <h3>Data Loading</h3>
                <p>
                  There are several data loaders for ZOOMA implemented out-of-the-box. These are available as one of the packages
                  in the zooma-loading module of the project tree. Checkout the ZOOMA source code from version control and take a
                  look at these for some examples, or email <a href="mailto:tburdett@ebi.ac.uk">Tony Burdett</a> for more
                  information.
                </p>
              </section>
              <section>
                <h3>ZOOMA URI Policies</h3>
                <p>
                  ZOOMA URIs are generated during data loading. Wherever possible, URIs from the originating database are used to
                  model data elements (biological entities, studies etc.) and the namespace is used to coin URIs that identify
                  ZOOMA concepts (annotations, provenance objects).
                </p>
                <p>
                  Below are listed the namespaces used in the current version of ZOOMA.
                </p>
                <table style={{fontSize: '0.85em'}}>
                  <tbody><tr>
                      <td>owl</td>
                      <td>http://www.w3.org/2002/07/owl#</td>
                    </tr>
                    <tr>
                      <td>rdfs</td>
                      <td>http://www.w3.org/2000/01/rdf-schema#</td>
                    </tr>
                    <tr>
                      <td>rdf</td>
                      <td>http://www.w3.org/1999/02/22-rdf-syntax-ns#</td>
                    </tr>
                    <tr>
                      <td>xsd</td>
                      <td>http://www.w3.org/2001/XMLSchema#</td>
                    </tr>
                    <tr>
                      <td>xml</td>
                      <td>http://www.w3.org/XML/1998/namespace</td>
                    </tr>
                    <tr>
                      <td>swrl</td>
                      <td>http://www.w3.org/2003/11/swrl#</td>
                    </tr>
                    <tr>
                      <td>swrlb</td>
                      <td>http://www.w3.org/2003/11/swrlb#</td>
                    </tr>
                    <tr>
                      <td>skos</td>
                      <td>http://www.w3.org/2004/02/skos/core#</td>
                    </tr>
                    <tr>
                      <td>dc</td>
                      <td>http://purl.org/dc/elements/1.1/</td>
                    </tr>
                    <tr>
                      <td>oac</td>
                      <td>http://www.openannotation.org/ns/</td>
                    </tr>
                    <tr>
                      <td>prov</td>
                      <td>http://www.w3.org/TR/prov-o/</td>
                    </tr>
                    <tr>
                      <td>zooma</td>
                      <td>www.ebi.ac.uk/spot/zooma/</td>
                    </tr>
                    <tr>
                      <td>zoomaterms</td>
                      <td>http://rdf.ebi.ac.uk/terms/zooma/</td>
                    </tr>
                    <tr>
                      <td>zoomaresource</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/</td>
                    </tr>
                    <tr>
                      <td>annotation</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/annotation/</td>
                    </tr>
                    <tr>
                      <td>propval</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/propertyvalue/</td>
                    </tr>
                    <tr>
                      <td>arrayexpress</td>
                      <td>http://www.ebi.ac.uk/arrayexpress/</td>
                    </tr>
                    <tr>
                      <td>aeresource</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/</td>
                    </tr>
                    <tr>
                      <td>aeannotation</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/annotation/</td>
                    </tr>
                    <tr>
                      <td>aeexperiment</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/arrayexpress/experiment/</td>
                    </tr>
                    <tr>
                      <td>aeproperty</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/arrayexpress//property/</td>
                    </tr>
                    <tr>
                      <td>gxa</td>
                      <td>http://www.ebi.ac.uk/gxa/</td>
                    </tr>
                    <tr>
                      <td>gxaresource</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/gxa/</td>
                    </tr>
                    <tr>
                      <td>gxaannotation</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/gxa/annotation/</td>
                    </tr>
                    <tr>
                      <td>gxaexperiment</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/gxa/experiment/</td>
                    </tr>
                    <tr>
                      <td>gxaproperty</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/gxa/property/</td>
                    </tr>
                    <tr>
                      <td>gwas</td>
                      <td>http://www.genome.gov/gwastudies/</td>
                    </tr>
                    <tr>
                      <td>gwasresource</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/gwas/</td>
                    </tr>
                    <tr>
                      <td>gwasannotation</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/gwas/annotation/</td>
                    </tr>
                    <tr>
                      <td>gwassnp</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/gwas/snp/</td>
                    </tr>
                    <tr>
                      <td>gwasproperty</td>
                      <td>http://rdf.ebi.ac.uk/resource/zooma/gwas/property/</td>
                    </tr>
                    <tr>
                      <td>efo</td>
                      <td>http://www.ebi.ac.uk/efo/</td>
                    </tr>
                    <tr>
                      <td>cl</td>
                      <td>http://purl.org/obo/owl/CL#</td>
                    </tr>
                    <tr>
                      <td>obo</td>
                      <td>http://purl.obolibrary.org/obo/</td>
                    </tr>
                    <tr>
                      <td>ncbitaxon</td>
                      <td>http://purl.org/obo/owl/NCBITaxon#</td>
                    </tr>
                    <tr>
                      <td>pubmed</td>
                      <td>http://europepmc.org/abstract/MED/</td>
                    </tr>
                  </tbody></table>
              </section>
            <ul id="secondary-nav">
                <li className="first"><Link to="/docs">Home</Link></li>
                <li><Link to="/docs/search">Getting Started</Link></li>
                <li><Link to="/docs/api">REST API Documentation</Link></li>
                <li className="last active"><Link to="/docs/developers">Developer Documentation</Link></li>
            </ul>
            </main>
    )
}
