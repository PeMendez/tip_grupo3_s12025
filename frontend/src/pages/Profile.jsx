import { useTitle } from "../contexts/TitleContext.jsx";
import { useEffect, useState } from "react";
import {deleteMember, getAllMembers, getHome} from "../api/homeService.js";
import './styles/profile.css';
import { FaCopy } from 'react-icons/fa';
import TextButton from "../components/TextButton.jsx";
import Toast from "../components/Toast.jsx";
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import {useAuth} from "../contexts/AuthContext.jsx";
import { getUserRoleInCurrentHome } from "../api/userHomeService.js";
import RoundButton from "../components/RoundButton.jsx";
import DeleteModal from "../components/DeleteModal.jsx";


const Profile = () => {
    const { setHeaderTitle } = useTitle();
    const [homeName, setHomeName] = useState('');
    const [key, setKey] = useState('');
    const [userRole, setUserRole] = useState('');
    const [members, setMembers] = useState([]);
    const [isEditing, setIsEditing] = useState(false);
    const [itemToDelete, setItemToDelete] = useState(null);
    const [homeId, setHomeId] = useState(null);
    const [password, setPassword] = useState({
        current: '',
        new: '',
        confirm: ''
    });
    const [toast, setToast] = useState({
        show: false,
        message: ''
    });

    const token = localStorage.getItem("token");
    const { user } = useAuth();
    const username = user?.sub;

    useEffect(() => {
        setHeaderTitle("Mi Perfil");
    }, [setHeaderTitle]);

    useEffect(() => {
        const fetchHome = async () => {
            try {
                const home = await getHome(token);
                setHomeName(home.name);
                setKey(home.key);
                setHomeId(home.id);

                const role = await getUserRoleInCurrentHome(home.id, token);
                setUserRole(role);

                const allMembers = await getAllMembers(token, home.id);
                setMembers(allMembers);

            } catch (error) {
                console.error("Error al obtener datos", error);
            }
        };
        fetchHome();
    }, []);

    const showToast = (message, type) => {
        setToast({
            show: true,
            message: message,
            type: type
        });

        setTimeout(() => {
            setToast(prev => ({...prev, show: false}));
        }, 3000);
    };

    const handleCopyKey = () => {
        navigator.clipboard.writeText(key);
        showToast("Clave copiada al portapapeles", "info");
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
            showToast("Las contraseñas no coinciden", "error");
            return;
        }
        // Password change logic here
        showToast("Contraseña cambiada con éxito", "success");
        setPassword({
            current: '',
            new: '',
            confirm: ''
        });
    };

    const handleEditMembers = () => {
        setIsEditing(!isEditing);
    };

    const handleConfirmDelete = async () => {
        if (!itemToDelete) return;
        try {
            await deleteMember(itemToDelete.homeId, itemToDelete.user.id, token);
            const updatedMembers = members.filter(
                m => m.user.id !== itemToDelete.user.id
            );
            setMembers(updatedMembers);
            showToast("Miembro eliminado correctamente", "success");
        } catch (error) {
            showToast("Error al eliminar miembro", "error");
            console.error("Delete error:", error);
        } finally {
            setItemToDelete(null);
            setIsEditing(false);
        }
    };
    return (
        <div className="profile-container">
            <BackOrCloseButton type="arrow"/>
            <section className="section">
                <h2 className="section-title">Información de la Casa</h2>

                <div className="info-group">
                    <span className="info-label">Nombre de la casa:</span>
                    <span className="info-value">{homeName}</span>
                </div>

                {userRole === 'ADMIN' && (
                    <div className="info-group">
                        <span className="info-label">Clave de la casa:</span>
                        <div className="key-container">
                            <code className="key-value">{key}</code>
                            <button
                                onClick={handleCopyKey}
                                className="copy-btn"
                                aria-label="Copiar clave"
                            >
                                <FaCopy className="copy-icon"/>
                            </button>
                        </div>
                        <p className="caption">
                            Comparte esta clave con otros usuarios para que puedan unirse a tu casa.
                        </p>
                    </div>
                )}
                <div className='info-home'>
                    <div className="section-header">
                        <h2 className="section-subtitle">Integrantes</h2>
                        {userRole === 'ADMIN' && (
                            <RoundButton
                                type="edit"
                                onClick={() => handleEditMembers()}
                            />
                        )}
                    </div>
                    <div className='home-container'>
                        <ul className={`members-list`}>
                            {members.map((member, index) => (
                                <li key={index} className="member-item">
                                    <div className="member-info">
                                             <span className="member-name">
                                                {member.user.username}
                                             </span>
                                    </div>

                                    {member.role === 'ADMIN' && (
                                        <span className="member-role admin">
                                            ADMIN
                                        </span>
                                    )}
                                    {isEditing && userRole === 'ADMIN' && member.role !== 'ADMIN' && (
                                        <TextButton
                                            text={"Eliminar"}
                                            onClick={() => setItemToDelete({
                                                user: member.user,
                                                homeId: homeId
                                            })}
                                        />
                                    )}
                                </li>
                            ))}
                        </ul>
                    </div>
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
            {toast.show && (
                <Toast
                    message={toast.message}
                    onClose={() => setToast(prev => ({...prev, show: false}))}
                    type={toast.type}
                />
            )}

            {itemToDelete && (
                <DeleteModal
                    device={itemToDelete}
                    onConfirm={handleConfirmDelete}
                    onCancel={() => setItemToDelete(null)}
                    message={`¿Estás seguro que querés eliminar a "${itemToDelete.user.username}" de tu casa?
                                Al confirmar todos sus dispositivos pasarán a desconfigurados`}
                />
            )}
        </div>
    );
};

export default Profile;