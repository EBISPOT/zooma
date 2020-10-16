

import React, { Component, Fragment } from 'react'

import { Row, Column } from 'react-foundation'



import { EBIFooter, EBIMasthead } from '../components/EBI'
import Lode from '../components/Lode'
import ZoomaNav from '../Navbar'


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
            <main>
              <Lode/>
            </main>
        )
    }

}