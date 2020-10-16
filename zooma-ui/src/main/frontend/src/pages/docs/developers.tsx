import { Link } from 'react-router-dom'
import React, { Fragment } from "react";
import { Row } from "react-foundation";
import { EBIMasthead, EBIFooter } from "../../components/EBI";
import ZoomaNav from "../../Navbar";

export default function developerDocs() {
    return (
            <main>
            <h3>Documentation</h3>
            <ul id="secondary-nav">
                <li className="first"><Link to="/docs">Home</Link></li>
                <li><Link to="/docs/search">Getting Started</Link></li>
                <li><Link to="/docs/api">REST API Documentation</Link></li>
                <li className="last active"><Link to="/docs/developers">Developer Documentation</Link></li>
            </ul>
            </main>
    )
}