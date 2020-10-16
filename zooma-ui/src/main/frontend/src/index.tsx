
import ReactDOM from 'react-dom';
import { BrowserRouter } from 'react-router-dom';
import * as React from 'react'
import App from './App'

ReactDOM.render((
  <BrowserRouter>
    <App />
  </BrowserRouter>
  ), document.getElementById('root')
);

window['ebiFrameworkInvokeScripts']()


