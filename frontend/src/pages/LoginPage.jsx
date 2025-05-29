import {useEffect, useState} from 'react'
import { useNavigate } from 'react-router-dom'
import LoginPopup from '../components/LoginPopUp'
import RegisterPopup from '../components/RegisterPopUp'
import logo from '../assets/NeoHub.png'
import './styles/loginPage.css'

function LoginPage({ setHeaderTitle }) {
    const [showLogin, setShowLogin] = useState(false)
    const [showRegister, setShowRegister] = useState(false)
    const navigate = useNavigate()

    useEffect(() => {
        setHeaderTitle("NeoHub - Login")
    }, [setHeaderTitle]);

    return (
        <div className="app-container">
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
                        navigate('/home')
                        setShowRegister(false)
                    }}
                />
            )}
        </div>
    )
}

export default LoginPage
