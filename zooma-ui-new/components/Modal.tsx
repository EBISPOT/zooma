

import { Component } from "react"

interface Props {
    title:string
}

interface State {
}

export default class Modal extends Component<Props, State> {

    constructor(props) {
        super(props)

        this.state = {
        }
    }

    render() {

        return (
            <div className="modal-outer">
                <div className="modal-inner">
                    <div className="modal-title">
                        <h1>{this.props.title}</h1>
                    </div>
                    <div className="modal-body">
                        {this.props.children}
                    </div>
                </div>
            </div>
        )
    }

}
