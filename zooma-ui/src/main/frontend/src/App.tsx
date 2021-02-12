
import * as React from 'react'
import Navbar from './Navbar'
import { EBIFooter, EBIMasthead } from './components/EBI';
import { Route } from 'react-router-dom';
import Home from './pages/Home';
import Docs from './pages/docs';
import DocsApi from './pages/docs/api';
import DocsSearch from './pages/docs/search';
import About from './pages/about';

export default function App() {
  return (
    <div className="App">
      <EBIMasthead/>
      <Navbar />
      <Route exact path='/' component={Home}></Route>
      <Route exact path='/docs' component={Docs}></Route>
      <Route exact path='/docs/search' component={DocsSearch}></Route>
      <Route exact path='/docs/api' component={DocsApi}></Route>
      <Route exact path='/about' component={About}></Route>
      <EBIFooter/>
    </div>
  );
}