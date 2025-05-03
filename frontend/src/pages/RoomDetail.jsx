import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import {
    FiSun, FiThermometer, FiShield, FiVideo, FiLock, FiEdit, FiPlus
} from 'react-icons/fi';
import { LuAirVent } from "react-icons/lu";
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import {
    getRoomDetails,
    addDeviceToRoom,
    deleteDevice
} from "../api/roomService";
import './roomDetail.css';

const deviceOptions = [
    { name: 'Luz', type: 'LIGHT', icon: <FiSun size={24} /> },
    { name: 'Temperatura', type: 'temperatureSensor', icon: <FiThermometer size={24} /> },
    { name: 'Alarma', type: 'ALARM', icon: <FiShield size={24} /> },
    { name: 'Aire Acondicionado', type: 'AIR_CONDITIONER', icon: <LuAirVent size={24} /> },
    { name: 'Control TV', type: 'TV_CONTROL', icon: <FiVideo size={24} /> },
    { name: 'Enchufe', type: 'smartOutlet', icon: <FiLock size={24} /> },
];

const RoomDetail = () => {
    const { id } = useParams();
    const token = localStorage.getItem('token');

    const [roomName, setRoomName] = useState("");
    const [devices, setDevices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [editMode, setEditMode] = useState(false);
    const [addMode, setAddMode] = useState(false);
    const [showDeletePopup, setShowDeletePopup] = useState(false);
    const [deviceToDelete, setDeviceToDelete] = useState(null);

    useEffect(() => {
        const fetchRoom = async () => {
            try {
                setLoading(true);
                const room = await getRoomDetails(id, token);
                setRoomName(room.name || "Habitación sin nombre");
                setDevices(room.devices || []);
            } catch (err) {
                console.error(err);
                setError("Error al cargar detalles de la habitación");
            } finally {
                setLoading(false);
            }
        };
        fetchRoom();
    }, [id, token]);

    const handleAddDevice = async (device) => {
        try {
            await addDeviceToRoom(id, device, token);
            const updatedRoom = await getRoomDetails(id, token);
            setDevices(updatedRoom.devices || []);
            setAddMode(false);
        } catch (err) {
            console.error("Error al agregar dispositivo", err);
        }
    };

    const handleConfirmDelete = async () => {
        if (!deviceToDelete) return;
        try {
            await deleteDevice(id, deviceToDelete.id, token);
            const updatedRoom = await getRoomDetails(id, token);
            setDevices(updatedRoom.devices || []);
            setShowDeletePopup(false);
            setDeviceToDelete(null);
        } catch (err) {
            console.error("Error al eliminar dispositivo", err);
        }
    };

    if (loading) return <div className="room-detail-container">Cargando...</div>;

    if (error) {
        return (
            <div className="room-detail-container">
                <BackOrCloseButton />
                <div className="error-message">{error}</div>
                <button onClick={() => window.location.reload()}>Reintentar</button>
            </div>
        );
    }

    if (addMode) {
        return (
            <div className="main-container">
                <div className="header-wrapper">
                    <div className="header">
                        <BackOrCloseButton type="arrow" onClick={() => setAddMode(false)}/>
                        <h2>Agregar Dispositivos</h2>
                    </div>
                </div>
                <div className="room-grid">
                    {deviceOptions.map((device, index) => (
                        <button
                            key={index}
                            onClick={() => handleAddDevice({
                                name: device.name,
                                type: device.type
                            })}
                            className="room-button"
                        >
                            <div className="device-icon">{device.icon}</div>
                            <span>{device.name}</span>
                        </button>
                    ))}
                </div>
            </div>
        );
    }

    if (editMode) {
        return (
            <div className="main-container">
                <div className="header-wrapper">
                    <div className="header">
                        <BackOrCloseButton type="arrow" onClick={() => setEditMode(false)}/>
                        <h2>Editar Dispositivos</h2>
                    </div>
                </div>
                <div className="room-grid">
                    {devices.map((device, index) => {
                        const iconMatch = deviceOptions.find(d => d.type === device.type);
                        return (
                            <div
                                key={index}
                                className="room-editable-container"
                                onClick={() => {
                                    setDeviceToDelete(device);
                                    setShowDeletePopup(true);
                                }}
                            >
                                <div className="room-button edit-mode">
                                    <div className="device-icon">{iconMatch?.icon || <FiSun size={24}/>}</div>
                                    <span>{device.name}</span>
                                    <div className="delete-icon-full">🗑️</div>
                                </div>
                            </div>
                        );
                    })}
                    <div className="add-room-icon">
                        <button onClick={() => {
                            setEditMode(false);
                            setAddMode(true);
                        }}>
                            <FiPlus size={24} className="icon"/>
                        </button>
                    </div>
                </div>

                {showDeletePopup && (
                    <div className="modal-backdrop" onClick={() => setShowDeletePopup(false)}>
                        <div className="modal" onClick={(e) => e.stopPropagation()}>
                            <p>¿Eliminar "{deviceToDelete?.name}"?</p>
                            <div className="modal-actions">
                                <button onClick={handleConfirmDelete}>Confirmar</button>
                                <button onClick={() => setShowDeletePopup(false)}>Cancelar</button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        );
    }

    return (
        <div className="main-container">
            <div className="header-wrapper">
                <div className="header">
                    <BackOrCloseButton/>
                    <h2>{roomName}</h2>
                    {devices.length > 0 && (
                        <button onClick={() => setEditMode(true)}>
                            <FiEdit size={24}/>
                        </button>
                    )}
                </div>
            </div>

            <div className="room-grid">
                {devices.length > 0 ? (
                    devices.map((device, index) => {
                        const iconMatch = deviceOptions.find(d => d.type === device.type);
                        return (
                            <div
                                key={index}
                                className="room-button"
                            >
                                <div className="device-icon">{iconMatch?.icon || <FiSun size={24}/>}</div>
                                <span>{device.name}</span>
                            </div>
                        );
                    })
                ) : (
                    <div className="no-devices">
                        <p>Aún no tenés dispositivos...</p>
                        <button onClick={() => setAddMode(true)}>
                            <FiPlus /> Agregar dispositivo
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default RoomDetail;