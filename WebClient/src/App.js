import { useState } from 'react'
import Navigator from './components/Navigator';
import { Toaster } from 'react-hot-toast';
import GetEvents from './pages/GetEvents';


function App() {

    const [page, setPage] = useState(<GetEvents/>);

    return (
        <div className="App" style={{'margin': '0'}}>
            <Toaster / >
            <Navigator setPage={setPage} / >
            {page}
        </div>
    );
  }
  
  export default App;