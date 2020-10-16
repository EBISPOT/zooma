
import * as React from 'react'
import Navbar from './Navbar'
import Main from './Main'
import { EBIFooter, EBIMasthead } from './components/EBI';

export default function App() {
  return (
    <div className="App">
      <EBIMasthead/>
      <Navbar />
      <Main />
      <EBIFooter/>
    </div>
  );
}