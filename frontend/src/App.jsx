import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import MainPage from './pages/MainPage'
import fondo from './assets/fondo.png';
import './App.css'
import RoomDetail from "./pages/RoomDetail.jsx";
import DeviceRules from './pages/DeviceRules.jsx'

function App() {
    return (
        <div
            className="app-container"
            style={{
                backgroundImage: `url(${fondo})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                minHeight: '100vh'
            }}
        >
            <Router>
                <Routes>
                    <Route path="/" element={<LoginPage/>}/>
                    <Route path="/home" element={<MainPage/>}/>
                    <Route path="/room/:id" element={<RoomDetail />} />
                    <Route path="/rule/:id" element={<DeviceRules />} />

                </Routes>
            </Router>
        </div>
            )
            }

export default App