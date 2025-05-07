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
import { getUnconfiguredDevices } from "../api/deviceService.js"

const RoomDetail = () => {
    const { id } = useParams();
    const token = localStorage.getItem('token');

    const [roomName, setRoomName] = useState("");
    const [devices, setDevices] = useState([]);
    const [availableDevices, setAvailableDevices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [editMode, setEditMode] = useState(false);
    const [addMode, setAddMode] = useState(false);
    const [showDeletePopup, setShowDeletePopup] = useState(false);
    const [deviceToDelete, setDeviceToDelete] = useState(null);

    const deviceTypeIcons = {
        LIGHT: <FiSun size={24} />,
        temperatureSensor: <FiThermometer size={24} />,
        ALARM: <FiShield size={24} />,
        AIR_CONDITIONER: <LuAirVent size={24} />,
        TV_CONTROL: <FiVideo size={24} />,
        smartOutlet: <FiLock size={24} />
    };

    const getDeviceIcon = (type) => deviceTypeIcons[type] || <FiSun size={24} />;

    useEffect(() => {
        const fetchRoom = async () => {
            try {
                setLoading(true);
                const room = await getRoomDetails(id, token);
                setRoomName(room.name || "Habitación sin nombre");
                setDevices(room.deviceList || []);
            } catch (err) {
                console.error(err);
                setError("Error al cargar detalles de la habitación");
            } finally {
                setLoading(false);
            }
        };
        fetchRoom();
    }, [id, token]);

    const handleAddClick = async () => {
        setEditMode(false);
        setAddMode(true);
        try {
            setLoading(true);
            const devices = await getUnconfiguredDevices(token);
            setAvailableDevices(devices || []);
        } catch (err) {
            console.error(err);
            setError("Error al cargar los dispositivos");
        } finally {
            setLoading(false);
        }
    };

    const handleAddDevice = async (device) => {
        try {
            await addDeviceToRoom(id, device.id, token);
            const updatedRoom = await getRoomDetails(id, token);
            setDevices(updatedRoom.deviceList || []);
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
            setDevices(updatedRoom.deviceList || []);
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
                    {availableDevices.map((device, index) => (
                        <button
                            key={index}
                            onClick={() => handleAddDevice(device)}
                            className="room-button"
                        >
                            <div className="device-icon">{getDeviceIcon(device.type)}</div>
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
                                    <div className="device-icon">{getDeviceIcon(device.type)}</div>
                                    <span>{device.name}</span>
                                    <div className="delete-icon-full">🗑️</div>
                                </div>
                            </div>
                        );
                    })}
                    <div className="add-device-icon">
                        <button onClick={handleAddClick}>
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
                </div>
            </div>
                    {devices.length > 0 && (
                        <div className="edit-container">
                            <div className="edit-button">
                                <button onClick={() => setEditMode(true)}>
                                    <FiEdit size={24}/>
                                </button>
                            </div>
                        </div>
                                )}

                                <div className="room-grid">
                                    {devices.length > 0 ? (
                                        devices.map((device, index) => {
                                            return (
                                                <div
                                                    key={index}
                                                    className="room-button"
                                                >
                                                    <div className="device-icon">{getDeviceIcon(device.type)}</div>
                                                    <span>{device.name}</span>
                                                </div>
                                            );
                                        })
                                    ) : (
                                        <div className="no-devices">
                                            <p>Aún no tenés dispositivos...</p>
                                            <button onClick={handleAddClick}>
                                                <FiPlus/> Agregar dispositivo
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </div>
                            );
                            };

                            export default RoomDetail;