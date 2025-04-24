import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import LoginPopup from '../components/LoginPopUp'
import RegisterPopup from '../components/RegisterPopUp'
import logo from '../assets/NeoHub.png'
import fondo from '../assets/fondo.png'
import './loginPage.css'

function LoginPage() {
    const [showLogin, setShowLogin] = useState(false)
    const [showRegister, setShowRegister] = useState(false)
    const navigate = useNavigate()

    return (
        <div className="app-container" style={{ backgroundImage: `url(${fondo})` }}>
            <div className="hero">
                <img src={logo} alt="Neo Hub Logo" className="logo" />
                <h1>Bienvenido a Neo Hub</h1>
                <p>Controlá tu hogar inteligente desde un solo lugar.</p>
                <div className="botones">
                    <button onClick={() => setShowLogin(true)}>Iniciar sesión</button>
                    <button onClick={() => setShowRegister(true)}>Registrarte</button>
                </div>
            </div>

            {showLogin && (
                <LoginPopup
                    onClose={() => setShowLogin(false)}
                    onSuccessLogin={() => {
                        navigate('/home')
                        setShowLogin(false)
                    }}
                />
            )}

            {showRegister && (
                <RegisterPopup
                    onClose={() => setShowRegister(false)}
                    onSuccessRegister={() => {
                        navigate('/home') // o redirigir a otra ruta que quieras
                        setShowRegister(false)
                    }}
                />
            )}
        </div>
    )
}

export default LoginPage
