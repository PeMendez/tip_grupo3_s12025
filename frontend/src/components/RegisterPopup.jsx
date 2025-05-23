import { useState } from 'react'
import { register } from '../api/authService.js'

function RegisterPopup({ onClose, onSuccessRegister }) {
    const [usernameOrEmail, setUsernameOrEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')

    const handleRegister = async () => {
        if (!usernameOrEmail || !password) {
            setError('Todos los campos son obligatorios.')
            return
        }

        try {
            const response = await register(usernameOrEmail, password)
            onSuccessRegister(response)
            onClose()
        } catch (err) {
            setError(err + 'Error en el registro. Intente de nuevo.')
        }
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
                {error && <div className="error-message">{error}</div>}
                <div className="modal-actions">
                    <button onClick={handleRegister}>Registrarse</button>
                    <button onClick={onClose}>Cancelar</button>
                </div>
            </div>
        </div>
    )
}

export default RegisterPopup
