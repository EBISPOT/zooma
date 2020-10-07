import Link from "next/link";
import React, { Fragment } from "react";
import { Row, Column } from "react-foundation";
import EBIMasthead, { EBIFooter, EBIScripts } from "../../components/EBI";
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
                        {JSON.stringify(examples['1'], null, 2)}
                        </pre>
                    </div>

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