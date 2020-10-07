import EBIMasthead, { EBIStyles, EBIScripts } from "../components/EBI";
import Head from "next/head";
import ZoomaNav from "../components/ZoomaNav";
import { Fragment } from "react";
import { Row, Column } from 'react-foundation'

export default function About() {

  return(
    <Fragment>
      <Head>
        <EBIStyles/>
      </Head>
      <EBIMasthead/>
      <ZoomaNav/>

      <main>
        <Row>
          <Column small={2} large={2}>about</Column>
          <Column small={2} large={2}>about</Column>
        </Row>
      </main>

      <EBIScripts/>
   </Fragment>
  )

}
