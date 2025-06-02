import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import MainPage from './pages/MainPage'
import './App.css'
import RoomDetail from "./pages/RoomDetail.jsx";
import RulesManager from "./pages/RulesManager.jsx";
import Header from "./components/Header.jsx";
import {useState, useEffect} from "react";
import { subscribeToPushNotifications } from './api/pushNotification.js';

const App = () => {
    const [headerTitle, setHeaderTitle] = useState("NeoHub");

    useEffect(() => {
        const handlePushSubscription = async () => {
            const token = localStorage.getItem('token');
            if (token) {
                await subscribeToPushNotifications();
            }
        };

        handlePushSubscription();
    }, []);

    return (
        <div className="app-container">
            <Router>
                <Header title={headerTitle}/>
                <Routes>
                    <Route path="/"
                           element={<LoginPage setHeaderTitle={setHeaderTitle}/>}/>
                    <Route path="/home"
                           element={<MainPage setHeaderTitle={setHeaderTitle}/>}/>
                    <Route path="/room/:id"
                           element={<RoomDetail setHeaderTitle={setHeaderTitle}/>}/>
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