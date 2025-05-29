import { useState } from 'react';
import './styles/loginPopup.css';
import { login } from '../api/authService';
import TextButton from "./TextButton.jsx";

function LoginPopup({ onClose, onSuccessLogin }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleLogin = async () => {
        try {
            const data = await login(username, password);
            localStorage.setItem('token', data.token);
            onSuccessLogin();
        } catch (err) {
            setError('Credenciales incorrectas');
            console.error(err);
        }
    };

    return (
        <div className="modal-backdrop">
            <div className="modal">
                <h2>Iniciar sesión</h2>
                <input
                    type="text"
                    name="username"
                    placeholder="Usuario o email"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
                <input
                    type="password"
                    name="password"
                    placeholder="Contraseña"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                {error && <div className="error-message">{error}</div>}
                <div className="modal-actions">
                    <TextButton text={"Entrar"} handleClick={handleLogin}/>
                    <TextButton text={"Cancelar"} handleClick={onClose}/>
                </div>
            </div>
        </div>
    );
}

export default LoginPopup;
