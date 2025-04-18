import { useState } from 'react';
import './App.css';
import LoginPopup from './components/LoginPopUp.jsx';
import logo from './assets/NeoHub.png';
import fondo from './assets/fondo.png'

function App() {
    const [showModal, setShowModal] = useState(false);

    return (
        <div className="app" style={{ backgroundImage: `url(${fondo})` }}>
            <div className="hero">
                <img src={logo} alt="Neo Hub Logo" className="logo" />
                <h1>Bienvenido a Neo Hub</h1>
                <p>Controlá tu hogar inteligente desde un solo lugar.</p>
                <button onClick={() => setShowModal(true)}>Iniciar sesión</button>
            </div>
            {showModal && <LoginPopup onClose={() => setShowModal(false)} />}
        </div>
    );
}

export default App;
