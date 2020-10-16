

import React, { Component, Fragment } from 'react'

import { Row, Column } from 'react-foundation'



import dynamic from 'next/dynamic'
import { EBIFooter, EBIMasthead, EBIScripts } from '../components/EBI'
import ZoomaNav from '../components/ZoomaNav'

const Lode = dynamic(
  () => import('../components/Lode'),
  { ssr: false }
)


interface Props {
}

interface State {
}

export default class Sparql extends Component<Props, State> {

    constructor(props) {
      super(props)

      this.state = {
      }
    }

    componentDidMount() {

    }

    render() {

        return (
          <Fragment>
            <EBIMasthead />
            <ZoomaNav />
            <main>
              <Lode/>
            </main>
            <EBIFooter />
            <EBIScripts />
          </Fragment>
        )
    }

}