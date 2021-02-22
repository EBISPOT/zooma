import Link from "next/link";
import React, { Fragment } from "react";
import { Row, Column } from "react-foundation";
import { EBIMasthead, EBIFooter, EBIScripts } from "../../components/EBI";
import ZoomaNav from "../../components/ZoomaNav";
import examples from '../../data/api-response-examples.json'

export default function docs() {
    return (
        <Fragment>
            <EBIMasthead />
            <ZoomaNav />
            <main>
                <Row>
                    <Column small={12} medium={12}>
                    <h2>REST API Documentation</h2>

                    <h3>Introduction</h3>

                    <p>
                        This page describes how to develop against the ZOOMA REST API to search for and retrieve ZOOMA
                        objects.
                    </p>

                    <p>
                        All requests should be made to the root URL of the Zooma API, which is not shown in the example
    requests. The root URL for the API is <code>www.ebi.ac.uk/spot/zooma/v2/api</code>.
                    </p>

                    <h3>Predicting Annotations</h3>
                    <p>
                        You can use Zooma to predict an ontology annotation given a property value (and optionally a property
                        type).
                    </p>
                    <h5>Example Request:</h5>
                    <p>
                        Predict an ontology annotation for the text value "mus musculus"
                    </p>

                    <div>
                    <pre>GET /services/annotate?propertyValue=mus+musculus</pre>
                    </div>

                    <br/>

                    <h5>Response:</h5>
                    <div>
                        <pre>
                        {JSON.stringify(examples['2'], null, 2)}
                        </pre>
                    </div>

                    <p>
                        This example predicts that 'mus musculus' should be annotated with the ontology term
                        <span className="uri">http://purl.obolibrary.org/obo/NCBITaxon_10090</span>. The confidence for this prediction is 'HIGH', and
                        derives from an existing annotation the URI <span className="uri">http://rdf.ebi.ac.uk/resource/zooma/gxa/90E386B39F0AD3DA5CCBD8AAA6F14907</span>.
                        This annotation was predicted based on curated mappings in the 'ExpressionAtlas (atlas)' database.
                    </p>


                    <h5>Additional Parameters:</h5>
                    <p>Zooma supports the option to specify the data sources it will search from. By default (specifying nothing), Zooma will search its available databases containing curated mappings
                        (and that do not include ontology sources), and if nothing is found it will look in the Ontology Lookup Service (OLS) to predict ontology annotations.
                    </p>
                    <p>
                        A set of filters can be applied to modify the default behavior:
                        <ul>
                                <li>required:[datasource1,datasource2,…]</li>
                                <li>preferred:[datasource2,datasource1,…]</li>
                                <li>ontologies:[efo,go,…]</li>
                        </ul>

                    <p>
                        where datasource1,datasource2,etc, are the database names of the datasources (see table below).
                    </p>

                        <ul>
                            <li><i>required</i> will limit the search to the given datasources</li>
                            <li><i>preferred</i> will provide a ranking for those datasources</li>
                            <li>and <i>ontologies</i> will limit the OLS search to the given ontologies</li>
                        </ul>
                    </p>
                    <p>If 'required:[none]' is specified, Zooma will search the OLS without looking into the datasources. If 'ontologies:[none]' is specified,
                        Zooma will not search the OLS if the datasource search fails to make any predictions.
                    </p>
                    <p>
                        In the table below you can see the available databases containing curated mappings in Zooma.<br/>
                        To define the source(s) you want Zooma to search in, use the <i>Database name</i> in the 'required:[]' field.<br/>
                        e.g. use 'required:[cttv]' to look into OpenTargets.
                    </p>
                    <pre>GET /services/annotate?propertyValue=disease&amp;filter=required:[cttv]</pre>
                    <table>
                        <tr>
                            <th>Display name</th>
                            <th>Database name</th>
                            <th>Learn more about the database's origin</th>
                        </tr>
                        <tr>
                            <td>OpenTargets</td>
                            <td>cttv</td>
                            <td><a href="//www.targetvalidation.org" target="_blank">www.targetvalidation.org</a></td>
                        </tr>
                        <tr>
                            <td>ClinVar</td>
                            <td>eva-clinvar</td>
                            <td><a href="//www.ebi.ac.uk/eva" target="_blank">www.ebi.ac.uk/eva</a></td>
                        </tr>
                        <tr>
                            <td>CellularPhenoTypes</td>
                            <td>sysmicro</td>
                            <td><a href="//www.ebi.ac.uk/fg/sym" target="_blank">www.ebi.ac.uk/fg/sym</a></td>
                        </tr>
                        <tr>
                            <td>ExpressionAtlas</td>
                            <td>atlas</td>
                            <td><a href="//www.ebi.ac.uk/gxa" target="_blank">www.ebi.ac.uk/gxa</a></td>
                        </tr>
                        <tr>
                            <td>EBiSC</td>
                            <td>ebisc</td>
                            <td><a href="//cells.ebisc.org/" target="_blank">www.cells.ebisc.org</a></td>
                        </tr>
                        <tr>
                            <td>UniProt</td>
                            <td>uniprot</td>
                            <td><a href="//www.ebi.ac.uk/uniprot" target="_blank">www.ebi.ac.uk/uniprot</a></td>
                        </tr>
                        <tr>
                            <td>GWAS</td>
                            <td>gwas</td>
                            <td><a href="//www.ebi.ac.uk/gwas/" target="_blank">www.ebi.ac.uk/gwas</a></td>
                        </tr>
                        <tr>
                            <td>CBI</td>
                            <td>cbi</td>
                            <td><a href="//www.ebi.ac.uk/biosamples/" target="_blank">www.ebi.ac.uk/biosamples</a></td>
                        </tr>
                        <tr>
                            <td>ClinVarXRefs</td>
                            <td>clinvar-xrefs</td>
                            <td><a href="//www.ncbi.nlm.nih.gov/clinvar" target="_blank">www.ncbi.nlm.nih.gov/clinvar</a></td>
                        </tr>
                    </table>

            <p>
                Predict an ontology annotation for the text value "mus musculus" and type "organism"
            </p>
            <pre>GET /services/annotate?propertyValue=mus+musculus&amp;propertyType=organism</pre>

            <p>
                Predict an ontology annotation for the text value "mus musculus" and type "organism" using annotations that are present in a defined list of datasources
            </p>
            <pre>GET /services/annotate?propertyValue=mus+musculus&amp;propertyType=organism&amp;filter=required:[atlas,gwas]</pre>

            <p>Predict an ontology annotation for the text value "ear inflorescence" using annotations that are present in a defined list of datasources</p>
            <pre>GET /services/annotate?propertyValue=ear+inflorescence&amp;filter=required:[sysmicro],ontologies:[none]</pre>
            <p>The 'ontologies:[none]' parameter will restrain Zooma from looking in the OLS if no annotation was found.</p>

            <p>
                Predict an ontology annotation for the text value "lung adenocarcinoma" using annotations
                that are present in a defined list of datasources, and specify a preference ranking for these
                datasources.
            </p>
            <pre>GET /services/annotate?propertyValue=lung+adenocarcinoma&amp;filter=required:[atlas,gwas],preferred:[gwas]</pre>
            <p>
                Here, the 'preferred' parameter allows you to set on order of preferred datasources
                (going from the datasource you trust the most, to the one you trust less) and this will affect the score.
            </p>

            <p>Predict an ontology annotation for the text value "mus musculus" and type "organism" using annotations that are present in a defined list of ontologies only</p>
            <pre>GET /services/annotate?propertyValue=mus+musculus&amp;propertyType=organism&amp;filter=required:[none],ontologies:[efo,mirnao]</pre>

            <h3>Resolving Resource URIs</h3>

            <p>
                Each resource in Zooma is given a URI as it's unique identifier.  The Zooma REST API takes compact URIs
                ("CURIEs") as it's parameters, so in order to lookup a resource you first need to convert a resource's
                URI to it's shortname.
            </p>

            <p>
                Zooma provides an API endpoint to do just this.  If we take an example property from our annotation above,
                with the URI <span className="uri">http://rdf.ebi.ac.uk/resource/zooma/461CE7ADFDA7E46E0AAEA17AC66AF143</span>,
                we can collapse this to it's shortform as follows
            </p>

            <h5>Request</h5>
            <pre>
POST /services/collapse
Content-Type: application/json
&#123;"uri":"http://rdf.ebi.ac.uk/resource/zooma/gxa/90E386B39F0AD3DA5CCBD8AAA6F14907"&#124;
            </pre>
            <h5>Response</h5>
            <pre>
                {JSON.stringify(examples['3'], null, 2)}
            </pre>

            <h3>Retrieving Resources</h3>

            <p>
                You can also use the Zooma API to fetch more information about each of the resource types in Zooma
                (which you may see in the above example when predicting annotations).  The following section describes
                how to retrieve more information about the key resource types.
            </p>

            <h4>Property Types</h4>

            <p>Retrieve all property types</p>
            <pre>GET /properties/types?limit=10</pre>

            <h5>Request</h5>
            <pre>
                {JSON.stringify(examples['4'], null, 2)}
            </pre>
            <h5>Response</h5>
            <pre>
                {JSON.stringify(examples['4'], null, 2)}
            </pre>

            <h4>Properties</h4>

            <p>Retrieve a property given it's shortname</p>
            <pre>GET /properties/zoomaresource:461CE7ADFDA7E46E0AAEA17AC66AF143</pre>

            <h5>Response</h5>
            <pre>
                {JSON.stringify(examples['5'], null, 2)}
            </pre>


            <h4>Annotations</h4>

            <p>
                The first query shown on this page, allows you to annotate your given property with a semantic tag.
                However, once you've retrieved an annotation prediction, you might be interested in getting more
                information about the annotation that prompted Zooma to predict your new mapping.  You can do this by
                using the "derivedFrom" relation and retrieving the annotation with the given ID.
            </p>

            <p>
                In the example above, we saw this:
            </p>
<pre>
...
derivedFrom: &#123;
    uri: "http://rdf.ebi.ac.uk/resource/zooma/gxa/90E386B39F0AD3DA5CCBD8AAA6F14907",
...
</pre>

            <p>We can shorten this as described in the section above, and then retrieve this annotation</p>

            <pre>GET /annotations/gxaresource:90E386B39F0AD3DA5CCBD8AAA6F14907</pre>

            <h5>Response</h5>
            <pre>
                {JSON.stringify(examples['6'], null, 2)}
            </pre>



                <h3>Documentation</h3>
                <ul id="secondary-nav">
                    <li className="first"><Link href="/docs">Home</Link></li>
                    <li><Link href="/docs/search">Getting Started</Link></li>
                    <li><Link href="/docs/api">REST API Documentation</Link></li>
                    <li className="last active"><Link href="/docs/developers">Developer Documentation</Link></li>
                </ul>
                    </Column>
                </Row>
            </main>
            <EBIFooter />
            <EBIScripts />
        </Fragment>
    )
}