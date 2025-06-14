import { useTitle } from "../contexts/TitleContext.jsx";
import { useEffect, useState } from "react";
import { getHome } from "../api/homeService2.js";
import { useSnackbar } from 'notistack';
import './styles/profile.css';
import { FaCopy } from 'react-icons/fa';
import TextButton from "../components/TextButton.jsx";

const Profile = () => {
    const { setHeaderTitle } = useTitle();
    const [homeName, setHomeName] = useState('');
    const [key, setKey] = useState('');
    const [username, setUsername] = useState('UsuarioEjemplo');
    const [password, setPassword] = useState({
        current: '',
        new: '',
        confirm: ''
    });

    useEffect(() => {
        setHeaderTitle("Mi Perfil");
    }, [setHeaderTitle]);

    useEffect(() => {
        const token = localStorage.getItem('token');
        const fetchHome = async () => {
            try {
                const home = await getHome(token);
                setHomeName(home.name);
                setKey(home.key);
            } catch (error) {
                console.error("Error al obtener datos", error);
                // Fallback for notification system
                alert("Error al cargar los datos");
            }
        };
        fetchHome();
    }, []);

    const handleCopyKey = () => {
        navigator.clipboard.writeText(key);
        alert("Clave copiada al portapapeles");
    };

    const handlePasswordChange = (e) => {
        const { name, value } = e.target;
        setPassword(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmitPassword = (e) => {
        e.preventDefault();
        if (password.new !== password.confirm) {
            alert("Las contraseñas no coinciden");
            return;
        }
        // Password change logic here
        alert("Contraseña cambiada con éxito");
        setPassword({
            current: '',
            new: '',
            confirm: ''
        });
    };

    return (
        <div className="profile-container">
            <section className="section">
                <h2 className="section-title">Información de la Casa</h2>

                <div className="info-group">
                    <span className="info-label">Nombre de la casa:</span>
                    <span className="info-value">{homeName}</span>
                </div>

                <div className="info-group">
                    <span className="info-label">Clave de la casa:</span>
                    <div className="key-container">
                        <code className="key-value">{key}</code>
                        <button
                            onClick={handleCopyKey}
                            className="copy-btn"
                            aria-label="Copiar clave"
                        >
                            <FaCopy className="copy-icon" />
                        </button>
                    </div>
                    <p className="caption">
                        Comparte esta clave con otros usuarios para que puedan unirse a tu casa.
                    </p>
                </div>
            </section>

            {/* Sección del Usuario */}
            <section className="section">
                <h2 className="section-title">Información del Usuario</h2>

                <div className="info-group">
                    <span className="info-label">Nombre de usuario:</span>
                    <span className="info-value">{username}</span>
                </div>

                <form onSubmit={handleSubmitPassword} className="password-form">
                    <h3 className="info-label">Cambiar contraseña</h3>

                    <div className="form-group">
                        <label className="form-label">Contraseña actual</label>
                        <input
                            className="form-input"
                            type="password"
                            name="current"
                            value={password.current}
                            onChange={handlePasswordChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Nueva contraseña</label>
                        <input
                            className="form-input"
                            type="password"
                            name="new"
                            value={password.new}
                            onChange={handlePasswordChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Confirmar nueva contraseña</label>
                        <input
                            className="form-input"
                            type="password"
                            name="confirm"
                            value={password.confirm}
                            onChange={handlePasswordChange}
                            required
                        />
                    </div>

                    <div className="btn-container">
                        <TextButton text={"Cambiar contraseña"} handleClick={handlePasswordChange}/>
                    </div>
                </form>
            </section>
        </div>
    );
};

export default Profile;