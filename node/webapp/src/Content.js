import React, {Component} from 'react';
import {routes} from "./routes";
import {HashRouter as Router, Switch} from "react-router-dom";
import PrivateRoute from "./PrivateRoute"


export default class Content extends Component {

    constructor() {
        super();
    }

    componentWillReceiveProps(nextProps) {
        console.log('Content: will receive');
    }


    render() {


        return (
            <div className='container'>
                <Router>
                    <Switch>
                        {routes.map(route => {
                            return (
                                <PrivateRoute
                                    key={route.path}
                                    path={route.path}
                                    component={route.component}/>
                            )
                        })
                        }
                    </Switch>
                </Router>
            </div>
        )
    }
}
