import { useTitle } from "../contexts/TitleContext.jsx";
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import Toast from "../components/Toast.jsx";
import { useAuth } from "../contexts/AuthContext.jsx";
import './styles/deviceConfig.css'
import TextButton from "../components/TextButton.jsx";
import { updateDevice } from "../api/deviceService.js";

const DeviceConfig = () => {
    const location = useLocation();
    const device = location.state?.device;
    const navigate = useNavigate();
    const { setHeaderTitle } = useTitle();
    const token = localStorage.getItem("token");
    const { user } = useAuth();
    const username = user?.sub;

    const [toast, setToast] = useState({ show: false, message: '' });
    const [editingName, setEditingName] = useState(false);
    const [editName, setEditName] = useState(device.name);
    const [savedName, setSavedName] = useState(device.name);
    const [isVisible, setIsVisible] = useState(device.visible);

    const isOwner = device.owner === username;

    useEffect(() => {
        setHeaderTitle(savedName);
    }, [setHeaderTitle, savedName]);

    const showToast = (message, type) => {
        setToast({ show: true, message, type: type });
        setTimeout(() => setToast({ show: false, message: '' }), 3000);
    };

    const handleNameChange = async () => {
        try {
            await updateDevice(device.id, {
                name: editName,
                visible: isVisible
            }, token);

            setSavedName(editName);
            showToast("Nombre actualizado", "success");
            setEditingName(false);
        } catch (e) {
            showToast("Error al actualizar nombre", "error");
            console.error(e);
        }
    };

    const handleVisibilityToggle = async () => {
        try {
            const newVisibility = !isVisible;
            setIsVisible(newVisibility);
            await updateDevice(device.id, {
                name: savedName,
                visible: newVisibility
            }, token);
            showToast("Visibilidad actualizada" ,"success");
        } catch (e) {
            showToast("Error al actualizar visibilidad", "error");
            console.error(e);
        }
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
                                value={editName}
                                onChange={(e) => setEditName(e.target.value)}
                            />
                        ) : (
                            <span className="info-value">{savedName}</span>
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
                    type={toast.type}
            />
            )}
        </div>
);
};

export default DeviceConfig;
