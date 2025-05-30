import {useEffect, useState} from 'react'
import { useNavigate } from 'react-router-dom'
import LoginPopup from '../components/LoginPopUp'
import RegisterPopup from '../components/RegisterPopUp'
import logo from '../assets/Logo1.svg'
import './styles/loginPage.css'
import TextButton from "../components/TextButton.jsx";

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
                    <TextButton text={"Iniciar sesión"} handleClick={() => setShowLogin(true)}/>
                    <TextButton text={"Registrarse"} handleClick={() => setShowRegister(true)}/>
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
