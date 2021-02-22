
import { Fragment } from 'react'
import Head from 'next/head'

import '../scss/styles.scss'

function ZoomaApp({ Component, pageProps }) {
  return (
    <Fragment>
        <Head>
          <meta name="HandheldFriendly" content="true" />
          <meta name="MobileOptimized" content="width" />
          <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
          <meta name="theme-color" content="#70BDBD" />
          <link rel="icon" type="image/x-icon" href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/favicon.ico" />
          <link rel="icon" type="image/png" href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/favicon-32x32.png" />
          <link rel="icon" type="image/png" sizes="192x192" href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/android-chrome-192x192.png" />
          <link rel="apple-touch-icon-precomposed" sizes="114x114" href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/apple-icon-114x114.png" /> 
          <link rel="apple-touch-icon-precomposed" sizes="72x72" href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/apple-icon-72x72.png" /> 
          <link rel="apple-touch-icon-precomposed" sizes="144x144" href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/apple-icon-144x144.png" /> 
          <link rel="apple-touch-icon-precomposed" href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/apple-icon-57x57.png" /> 
          <link rel="mask-icon" href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/safari-pinned-tab.svg" color="#ffffff" />
          <meta name="msapplication-TileColor" content="#2b5797" /> 
          <meta name="msapplication-TileImage" content="//ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/mstile-144x144.png" />
          <link rel="stylesheet" href="https://dev.ebi.emblstatic.net/web_guidelines/EBI-Icon-fonts/v1.2/fonts.css"/>
          <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
          <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js"></script>
          <script type="text/javascript" src="codemirror/codemirror.js"></script>
          <script type="text/javascript" src="codemirror/sparql.js"></script>
          <script type="text/javascript" src="scripts/lode.js"></script>
          <script type="text/javascript" src="scripts/thickbox.js"></script>
          <link rel="stylesheet" href="css/lode-style.css" type="text/css" media="screen" />
          <link rel="stylesheet" href="css/thickbox.css" type="text/css" media="screen" />
          <link rel="stylesheet" href="codemirror/codemirror.css" type="text/css" media="screen" />
        </Head>
        <Component {...pageProps} />
    </Fragment>
  )
}

export default ZoomaApp
