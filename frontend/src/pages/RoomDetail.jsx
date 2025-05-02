import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { FiSun, FiThermometer, FiShield, FiVideo, FiLock } from 'react-icons/fi';
import { LuAirVent } from "react-icons/lu";
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import { getRoomDetails, addDeviceToRoom } from "../api/roomService";
import './roomDetail.css';

const RoomDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [devices, setDevices] = useState([]);
    const [showDeviceOptions, setShowDeviceOptions] = useState(false);
    const [roomName, setRoomName] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const deviceOptions = [
        { name: 'Luz', type: 'LIGHT', icon: <FiSun size={24} /> },
        { name: 'Temperatura', type: 'THERMOMETER', icon: <FiThermometer size={24} /> },
        { name: 'Alarma', type: 'ALARM', icon: <FiShield size={24} /> },
        { name: 'Aire Acondicionado', type: 'AIR_CONDITIONER', icon: <LuAirVent size={24} /> },
        { name: 'Control TV', type: 'TV_CONTROL', icon: <FiVideo size={24} /> },
        { name: 'Enchufe', type: 'SOCKET', icon: <FiLock size={24} /> },
    ];

    const token = localStorage.getItem('token');

    useEffect(() => {
        const fetchRoomData = async () => {
            try {
                setLoading(true);
                setError(null);
                const roomData = await getRoomDetails(id, token);

                if (!roomData) {
                    throw new Error('No se recibieron datos de la habitación');
                }

                setRoomName(roomData.name || 'Habitación sin nombre');
                setDevices(Array.isArray(roomData.devices) ? roomData.devices : []);
            } catch (error) {
                console.error('Error fetching room details:', error);
                setError('Error al cargar los detalles de la habitación');
            } finally {
                setLoading(false);
            }
        };

        fetchRoomData();
    }, [id, token, navigate]);

    const handleAddDevice = async (device) => {
        try {
            setError(null);
            const updatedRoom = await addDeviceToRoom(id, device, token);

            if (!updatedRoom || !Array.isArray(updatedRoom.devices)) {
                throw new Error('Respuesta inválida al agregar dispositivo');
            }

            setDevices(updatedRoom.devices);
            setShowDeviceOptions(false);
        } catch (error) {
            console.error('Error adding device:', error);
            setError('Error al agregar el dispositivo');
        }
    };

    const getDeviceStatusClass = (status) => {
        if (!status) return 'device-status unknown';
        try {
            return `device-status ${status.toLowerCase()}`;
        } catch {
            return 'device-status unknown';
        }
    };

    if (loading) {
        return <div className="room-detail-container">Cargando...</div>;
    }

    if (error) {
        return (
            <div className="room-detail-container">
                <BackOrCloseButton />
                <div className="error-message">{error}</div>
                <button onClick={() => window.location.reload()}>Reintentar</button>
            </div>
        );
    }

    return (
        <div className="room-detail-container">
            <div className="header">
                <BackOrCloseButton/>
                <h2>{roomName}</h2>
            </div>

            {devices.length === 0 && !showDeviceOptions ? (
                <div className="no-devices">
                    <p>Aún no tenés dispositivos en esta habitación.</p>
                    <button
                        className="add-device-btn"
                        onClick={() => setShowDeviceOptions(true)}
                    >
                        Agregar dispositivo
                    </button>
                </div>
            ) : (
                <div className="device-grid">
                    {devices.map((device, index) => {
                        const deviceOption = deviceOptions.find(d => d.type === device.type);
                        const status = device.status || 'unknown';

                        return (
                            <div key={index} className="device-card">
                                <div className="device-icon">
                                    {deviceOption ? deviceOption.icon : <FiSun size={24} />}
                                </div>
                                <div className="device-name">{device.name || 'Dispositivo sin nombre'}</div>
                                <div className={getDeviceStatusClass(status)}>
                                    {status}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {showDeviceOptions && (
                <div className="device-options-overlay">
                    <div className="device-options">
                        <h3>Selecciona un dispositivo</h3>
                        <div className="device-options-grid">
                            {deviceOptions.map((device, index) => (
                                <button
                                    key={index}
                                    className="device-option-btn"
                                    onClick={() => handleAddDevice({
                                        name: device.name,
                                        type: device.type
                                    })}
                                >
                                    <div className="device-icon">{device.icon}</div>
                                    <div className="device-name">{device.name}</div>
                                </button>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RoomDetail;