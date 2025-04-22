import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import LoginPopup from '../components/LoginPopUp'
import logo from '../assets/NeoHub.png'
import fondo from '../assets/fondo.png'

function LoginPage() {
    const [showModal, setShowModal] = useState(false)
    const navigate = useNavigate()

    return (
        <div className="app-container" style={{ backgroundImage: `url(${fondo})` }}>
            <div className="hero">
                <img src={logo} alt="Neo Hub Logo" className="logo" />
                <h1>Bienvenido a Neo Hub</h1>
                <p>Controlá tu hogar inteligente desde un solo lugar.</p>
                <button onClick={() => setShowModal(true)}>Iniciar sesión</button>
            </div>
            {showModal && (
                <LoginPopup
                    onClose={() => setShowModal(false)}
                    onSuccessLogin={() => {
                        navigate('/home')
                        setShowModal(false)
                    }}
                />
            )}
        </div>
    )
}

export default LoginPage