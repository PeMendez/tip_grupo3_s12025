import { useState } from 'react'
import { register } from '../api/authService.js'
import TextButton from "./TextButton.jsx";

function RegisterPopup({ onClose, onSuccessRegister }) {
    const [usernameOrEmail, setUsernameOrEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [action, setAction] = useState('CREATE'); // 'CREATE' o 'JOIN'
    const [homeName, setHomeName] = useState('');
    const [accessKey, setAccessKey] = useState('');

    const handleRegister = async () => {

        if (!usernameOrEmail || !password || !confirmPassword || !homeName) {
            setError('Todos los campos son obligatorios.');
            return;
        }

        if (password !== confirmPassword) {
            setError('Las contraseñas no coinciden.');
            return;
        }

        if (action === 'JOIN' && (!homeName || !accessKey)) {
            setError('Para unirte a una home necesitas el nombre y la clave de acceso.');
            return;
        }

        try {
            const response = await register({
                username: usernameOrEmail,
                password: password,
                confirmPassword: confirmPassword,
                action: action,
                homeName: homeName,
                accessKey: action === 'CREATE' ? generateRandomKey() : accessKey
            });
            onSuccessRegister(response);
            onClose();
        } catch (err) {
            setError(err.response?.data?.message || 'Error en el registro. Intente de nuevo.');
            console.log(err);
        }
    }

    const generateRandomKey = () => {
        return Math.random().toString(36).substring(2, 10).toUpperCase();
    }

    return (
        <div className="modal-backdrop">
            <div className="modal">
                <h2>Registrarse</h2>
                <input
                    type="text"
                    placeholder="Username o Email"
                    value={usernameOrEmail}
                    onChange={(e) => setUsernameOrEmail(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="Contraseña"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="Confirme su contraseña"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                />

                <div className="action-selector">
                    <label>
                        <input
                            type="radio"
                            checked={action === 'CREATE'}
                            onChange={() => setAction('CREATE')}
                        />
                        Crear una nueva home
                    </label>
                    <label>
                        <input
                            type="radio"
                            checked={action === 'JOIN'}
                            onChange={() => setAction('JOIN')}
                        />
                        Unirse a una home existente
                    </label>
                </div>

                {action === 'JOIN' && (
                    <>
                        <input
                            type="text"
                            placeholder="Nombre de la home"
                            value={homeName}
                            onChange={(e) => setHomeName(e.target.value)}
                        />
                        <input
                            type="text"
                            placeholder="Clave de acceso"
                            value={accessKey}
                            onChange={(e) => setAccessKey(e.target.value)}
                        />
                    </>
                )}
                {action === 'CREATE' && (
                    <>
                        <input
                            type="text"
                            placeholder="Nombre de la home"
                            value={homeName}
                            onChange={(e) => setHomeName(e.target.value)}
                        />
                    </>
                )}

                {error && <div className="error-message">{error}</div>}
                <div className="modal-actions">
                    <TextButton text={"Registrarse"} handleClick={handleRegister}/>
                    <TextButton text={"Cancelar"} handleClick={onClose}/>
                </div>
            </div>
        </div>
    )
}

export default RegisterPopup