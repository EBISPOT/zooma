
import { ReactNode } from 'react'
import Link from 'next/link'
import { useRouter, Router } from "next/router"
import { Row, Column } from 'react-foundation'

export interface NavProps {
}

export default function ZoomaNav(props:NavProps) {

    let router = useRouter()

    return (
        <header id="masthead" className="masthead ontotools-masthead">
            <Row className="masthead-inner">
                <Column small={12} medium={12} id="local-title">
                    <h1>
                        <Link href="/">
                            <a title="Back to ZOOMA homepage">
                                <img className="ontotools-logo" src="/zooma_logo_new.png" />
                            </a>
                        </Link>
                    </h1>
                </Column>
                <nav>
                    <ul id="local-nav" className="dropdown menu float-left" data-description="navigational">

                        <li className={router.pathname === '/' ? 'active' : ''}>
                            <Link href="/">
                                <a>Home</a>
                            </Link>
                        </li>

                        <li className={router.pathname === '/sparql' ? 'active' : ''}>
                            <Link href="/sparql">
                                <a>Explore</a>
                            </Link>
                        </li>

                        <li className={router.pathname.startsWith('/docs') ? 'active' : ''}>
                            <Link href="/docs">
                                <a>Help</a>
                            </Link>
                        </li>

                        <li className={router.pathname === '/about' ? 'active' : ''}>
                            <Link href="/about">
                                <a>About</a>
                            </Link>
                        </li>
                    </ul>
                </nav>
            </Row>
        </header>
    )
}

