import React, {Fragment} from 'react';
import {ConnectedRouter} from 'react-router-redux'
import {reduxHistory} from "./store";
import DocumentTitle from 'react-document-title';
import {enquireScreen} from 'enquire-js';

import Header from './components/Header';
import Content from './Content';
import './styles/style';


let isMobile;

enquireScreen((b) => {
    isMobile = b;
});

class MainWrap extends React.PureComponent {
    state = {
        isMobile,
    }

    componentDidMount() {
        enquireScreen((b) => {
            this.setState({
                isMobile: !!b,
            });
        });
    }

    render() {
        return (
            <DocumentTitle title="VictorLink Operation UI">
                <ConnectedRouter history={reduxHistory}>
                    <Fragment>
                        <Header isMobile={this.state.isMobile}/>
                        <Content/>

                    </Fragment>
                </ConnectedRouter>
            </DocumentTitle>
        );
    }
}

export default MainWrap;
