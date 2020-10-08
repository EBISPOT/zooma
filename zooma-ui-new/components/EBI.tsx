
import { Fragment, Component } from "react"
import Head from "next/head"

export function EBIMasthead() {
    return (
        <header id="masthead-black-bar" className="clearfix masthead-black-bar">
        </header>
    )
}

export function EBIFooter() {
    return (
        <footer>
            <div id="global-footer" className="global-footer">
            <nav id="global-nav-expanded" className="global-nav-expanded row">
            </nav>
            <section id="ebi-footer-meta" className="ebi-footer-meta row">
            </section>
            </div>
        </footer>
    )
}

export class EBIScripts extends Component {
    constructor(props) {
        super(props)
    }

    render() {
        return (
            <Fragment>
                <script>document.body.dataset.ebiframeworkinvokescripts=false;</script>
                <script src="//ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/js/script.js"></script>
            </Fragment>
        )
    }

    componentDidMount() {
        //if(!document.body.classList.contains('ebi-black-bar-loaded')) {
            window['ebiFrameworkInvokeScripts']()
        //}
    }
}

