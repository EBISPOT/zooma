import * as React from 'react';
import { Switch, Route } from 'react-router-dom';

import Home from './pages/Home';
import Docs from './pages/docs'
import About from './pages/about'

export default function Main() {
  return (
    <Switch>
      <Route exact path='/' component={Home}></Route>
      <Route exact path='/docs' component={Docs}></Route>
      <Route exact path='/about' component={About}></Route>
    </Switch>
  );
}

