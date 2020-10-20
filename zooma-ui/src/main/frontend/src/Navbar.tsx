
import { Row, Column } from 'react-foundation'
import { Link, useLocation } from "react-router-dom";
import * as React from 'react'

export interface NavProps {
}

export default function Navbar(props:NavProps) {

    let loc = useLocation()

    return (
        <header id="masthead" className="masthead ontotools-masthead">
            <Row className="masthead-inner">
                <Column small={12} medium={12} id="local-title">
                    <h1>
                        <Link to="/" title="Back to ZOOMA homepage">
                            <img className="ontotools-logo" src={process.env.PUBLIC_URL + "images/zooma_logo_new.png"} />
                        </Link>
                    </h1>
                </Column>
                <nav>
                    <ul id="local-nav" className="dropdown menu float-left" data-description="navigational">

                        <li className={loc.pathname === '/' ? 'active' : ''}>
                            <Link to="/">
                                Home
                            </Link>
                        </li>

                        <li className={loc.pathname === '/sparql' ? 'active' : ''}>
                            <Link to="/sparql">
                                Explore
                            </Link>
                        </li>

                        <li className={loc.pathname.startsWith('/docs') ? 'active' : ''}>
                            <Link to="/docs">
                                Help
                            </Link>
                        </li>

                        <li className={loc.pathname === '/about' ? 'active' : ''}>
                            <Link to="/about">
                                About
                            </Link>
                        </li>
                    </ul>
                </nav>
            </Row>
        </header>
    )
}

