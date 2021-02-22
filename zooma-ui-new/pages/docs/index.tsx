import Link from "next/link";
import React, { Fragment } from "react";
import { Row } from "react-foundation";
import { EBIMasthead, EBIFooter, EBIScripts } from "../../components/EBI";
import ZoomaNav from "../../components/ZoomaNav";


export default function docs() {
    return (
        <Fragment>
            <EBIMasthead />
            <ZoomaNav />
            <main>
                <Row>
                    <Link href="/docs/search">
                        <div style={{cursor: 'pointer'}}>
                            <h2>Searching ZOOMA</h2>
                            <p>Check here for an overview on how to use Zooma, and how to search for ontology mappings given metadata descriptions</p>
                        </div>
                    </Link>
                </Row>
                <Row>
                    <Link href="/docs/api">
                        <div style={{cursor: 'pointer'}}>
                            <h2>Developer API</h2>
                            <p>Zooma developer documentation, including how to code tools against the Zooma REST API.</p>
                        </div>
                    </Link>
                </Row>
            </main>
            <EBIFooter />
            <EBIScripts />
        </Fragment>
    )
}