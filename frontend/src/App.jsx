import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import MainPage2 from './pages/MainPage2.jsx'
import './App.css'
import RoomDetail2 from "./pages/RoomDetail2.jsx";
import RulesManager from "./pages/RulesManager.jsx";
import Header from "./components/Header.jsx";
import {useState} from "react";


const App = () => {
    const [headerTitle, setHeaderTitle] = useState("NeoHub");

    return (
        <div className="app-container">
            <Router>
                <Header title={headerTitle}/>
                <Routes>
                    <Route path="/"
                           element={<LoginPage setHeaderTitle={setHeaderTitle}/>}/>
                    <Route path="/home"
                           element={<MainPage2 setHeaderTitle={setHeaderTitle}/>}/>
                    <Route path="/room/:id"
                           element={<RoomDetail2 setHeaderTitle={setHeaderTitle}/>}/>
                    <Route path="/rules"
                           element={<RulesManager setHeaderTitle={setHeaderTitle} />}/>
                    <Route path="/rule/:id"
                           element={<RulesManager isDeviceContext={true} setHeaderTitle={setHeaderTitle}/>}/>
                </Routes>
            </Router>
        </div>
    )
}

export default App