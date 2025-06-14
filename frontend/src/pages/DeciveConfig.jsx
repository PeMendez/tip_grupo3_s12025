import { useTitle } from "../contexts/TitleContext.jsx";
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import Toast from "../components/Toast.jsx";
import { useAuth } from "../contexts/AuthContext.jsx";
import './styles/deviceConfig.css'
import TextButton from "../components/TextButton.jsx";

const DeviceConfig = () => {
    const location = useLocation();
    const device = location.state?.device;
    const navigate = useNavigate();
    const { setHeaderTitle } = useTitle();
    const { user } = useAuth();
    const username = user?.sub;

    const [toast, setToast] = useState({ show: false, message: '' });
    const [newName, setNewName] = useState(device.name);
    const [editingName, setEditingName] = useState(false);
    const [isVisible, setIsVisible] = useState(device.visible);

    const isOwner = device.owner === username;

    useEffect(() => {
        console.log(username)
        setHeaderTitle(device.name);
    }, [setHeaderTitle, device.name]);

    const showToast = (message) => {
        setToast({ show: true, message });
        setTimeout(() => setToast({ show: false, message: '' }), 3000);
    };

    const handleNameChange = () => {
        // Lógica para guardar nuevo nombre en backend
        showToast("Nombre actualizado");
        setEditingName(false);
    };

    const handleVisibilityToggle = () => {
        setIsVisible(!isVisible);
        // Lógica para guardar visibilidad en backend
        showToast("Visibilidad actualizada");
    };

    return (
        <div className="profile-container">
            <BackOrCloseButton type="arrow" onClick={() => navigate(-1)} />

            <section className="section">
                <h2 className="section-title">Información del dispositivo</h2>

                <div className="info-group">
                    <span className="info-label">Nombre:</span>
                    <div className="name-and-button">
                        {editingName ? (
                            <input
                                className="form-input"
                                value={newName}
                                onChange={(e) => setNewName(e.target.value)}
                            />
                    ) : (
                        <span className="info-value">{device.name}</span>
                    )}
                    {isOwner && (
                        <div className="edit-btn">
                            {editingName ? (
                                <TextButton text={"Guardar"} handleClick={handleNameChange}/>
                            ) : (
                                <TextButton text={"Editar"} handleClick={() => setEditingName(true)}/>
                            )}
                        </div>
                    )}
                </div>
                </div>


                <div className="info-group">
                    <span className="info-label">Propietario:</span>
                    <span className="info-value">{device.owner}</span>
                </div>

                {isOwner && (
                    <div className="info-group">
                        <div className="styled-checkbox-container">
                            <label className="styled-checkbox">
                                <input
                                    type="checkbox"
                                    checked={isVisible}
                                    onChange={handleVisibilityToggle}
                                />
                                <span className="checkmark"></span>
                                <span className="checkbox-label">Hacer visible para otros usuarios</span>
                            </label>
                        </div>
                    </div>
                        )}
                    </section>

                {toast.show && (
                    <Toast
                    message={toast.message}
                onClose={() => setToast({show: false, message: ''})}
                type="info"
            />
            )}
        </div>
);
};

export default DeviceConfig;
