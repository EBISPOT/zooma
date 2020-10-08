import Link from "next/link";
import React, { Fragment } from "react";
import { Row } from "react-foundation";
import { EBIMasthead, EBIFooter, EBIScripts } from "../../components/EBI";
import ZoomaNav from "../../components/ZoomaNav";

export default function developerDocs() {
    return (
        <Fragment>
            <EBIMasthead />
            <ZoomaNav />
            <main>
            </main>
            <h3>Documentation</h3>
            <ul id="secondary-nav">
                <li className="first"><Link href="/docs">Home</Link></li>
                <li><Link href="/docs/search">Getting Started</Link></li>
                <li><Link href="/docs/api">REST API Documentation</Link></li>
                <li className="last active"><Link href="/docs/developers">Developer Documentation</Link></li>
            </ul>
            <EBIFooter />
            <EBIScripts />
        </Fragment>
    )
}