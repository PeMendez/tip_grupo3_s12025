import './LoginPopup.css';

function LoginPopup({ onClose }) {
    return (
        <div className="modal-backdrop">
            <div className="modal">
                <h2>Iniciar sesión</h2>
                <input type="text" placeholder="Usuario o email" />
                <input type="password" placeholder="Contraseña" />
                <div className="modal-actions">
                    <button className="login">Entrar</button>
                    <button onClick={onClose}>Cancelar</button>
                </div>
            </div>
        </div>
    );
}

export default LoginPopup;
