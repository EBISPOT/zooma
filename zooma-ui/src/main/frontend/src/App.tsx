
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

      <div style={{width: '100%', backgroundColor: '#00d9ff', padding: '30px', textAlign: 'center', fontSize: 'large'}}>
        <p style={{color: 'black'}}>
          If you’ve ever found our data helpful, please take our impact survey (15 min). Your replies will help keep the data flowing to the scientific community.
        </p>
        <br />
        <a style={{display: 'inline-block', cursor: 'pointer', outline: 'none', border: 'none'}} href="https://www.surveymonkey.co.uk/r/EMBL-EBI_Impact_DR">
          <span style={{color: 'black', background: 'none', border: '1px solid black', borderRadius: '8px', padding: '12px'}}>Take Survey</span>
        </a>
      </div>

      <Route exact path='/' component={Home}></Route>
      <Route exact path='/docs' component={Docs}></Route>
      <Route exact path='/docs/search' component={DocsSearch}></Route>
      <Route exact path='/docs/api' component={DocsApi}></Route>
      <Route exact path='/about' component={About}></Route>
      <EBIFooter/>
    </div>
  );
}
