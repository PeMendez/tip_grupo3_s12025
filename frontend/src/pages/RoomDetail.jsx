import {useState, useEffect, useCallback, useRef} from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
    FiSun, FiThermometer, FiShield, FiVideo, FiLock, FiEdit, FiPlus
} from 'react-icons/fi';
import { LuAirVent } from "react-icons/lu";
import { TiLightbulb } from "react-icons/ti";
import { MdOutlineBrightness4 } from "react-icons/md";
import warning from '../assets/warning.png'
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import {
    getRoomDetails,
    addDeviceToRoom,
    deleteDevice
} from "../api/roomService";
import './styles/roomDetail.css';
import {dimmerCommand, getUnconfiguredDevices, smartOutletCommand} from "../api/deviceService.js";
import { connectWebSocket } from "../api/websocket.js";
import Toast from '../components/Toast.jsx'

const RoomDetail = ({ setHeaderTitle }) => {
    const { id } = useParams();
    const token = localStorage.getItem('token');

    const [roomName, setRoomName] = useState("");
    const [devices, setDevices] = useState([]);
    const [availableDevices, setAvailableDevices] = useState([]);
    const [_, setError] = useState(null);
    const [editMode, setEditMode] = useState(false);
    const [addMode, setAddMode] = useState(false);
    const [showDeletePopup, setShowDeletePopup] = useState(false);
    const [deviceToDelete, setDeviceToDelete] = useState(null);
    const [toast, setToast] = useState(null);
    const audioContextRef = useRef(null);
    const [audioEnabled, setAudioEnabled] = useState(false);
    const navigate = useNavigate();

    const handleDeviceClick = (device) => {
        navigate(`/rule/${device.id}`);
    };

    const initAudio = () => {
        if (!audioContextRef.current) {
            audioContextRef.current = new (window.AudioContext || window.webkitAudioContext)();
            setAudioEnabled(true);
        }
    };

    //const isAlarmActive = devices.find(device => device.type === 'alarm')?.isActive || false;

    const deviceTypeIcons = {
        smart_outlet: <TiLightbulb size={24} />,
        temperature_sensor: <FiThermometer size={24} />,
        opening_sensor: <FiShield size={24} />,
        AIR_CONDITIONER: <LuAirVent size={24} />,
        TV_CONTROL: <FiVideo size={24} />,
        smartOutlet: <FiLock size={24} />,
        dimmer: <MdOutlineBrightness4 size={24} />
    };

    const getDeviceIcon = (type) => deviceTypeIcons[type] || <FiSun size={24} />;

    useEffect(() => {
        const handleFirstClick = () => {
            initAudio();
            document.removeEventListener('click', handleFirstClick);
        };

        document.addEventListener('click', handleFirstClick);

        return () => {
            document.removeEventListener('click', handleFirstClick);
        };
    }, []);

    const playAlarmSound = useCallback(() => {
        /*if (!audioEnabled || !audioContextRef.current) return;

        try {*/
            const audioContext = audioContextRef.current;
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();

            oscillator.type = "sine";
            oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
            gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);

            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);

            gainNode.gain.exponentialRampToValueAtTime(0.3, audioContext.currentTime + 0.1);
            gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 0.2);

            oscillator.start();

            setTimeout(() => {
                oscillator.stop();
            }, 5000);

        /*} catch (e) {
            console.error("Error al crear audio:", e);
        }*/
    }, [audioEnabled]);

    const toggleLight = async (device) => {
        try {
            console.log('Enviando comando para cambiar estado: ', !device.status);
            await smartOutletCommand(device, !device.status, token);
            console.log('Esperando websocket...');
        } catch (err) {
            console.error(err);
        }
    };

    const setBrightness = async (device, brightness) =>{
        try {
            console.log('Enviando comando para setear el brillo a: ', brightness);
            await dimmerCommand(device, brightness, token);
            console.log('Esperando websocket...');
        } catch (err) {
            console.error(err);
        }
    };

    const showNotification = (title, message, options = {}) => {
        const {
            icon = "https://cdn-icons-png.flaticon.com/512/619/619153.png",
            duration = 3000,
            toastClass = '',
            onClickRedirect = null
        } = options;

        if (Notification.permission === "granted") {
            const notification = new Notification(title, {
                body: message,
                icon: icon
            });
            if (onClickRedirect) {
                notification.onclick = () => {
                    navigate(onClickRedirect);
                    window.focus();
                };
                if (onClickRedirect) {
                    notification.onclick = () => {
                        navigate(onClickRedirect);
                        window.focus();
                    };
                }
            }
        } else if (Notification.permission !== "denied") {
            Notification.requestPermission().then(permission => {
                if (permission === "granted") {
                    new Notification(title, {
                        body: message,
                        icon: icon
                    });
                }
            });
        }

        setToast({
            message: message,
            key: Date.now(),
            duration: duration,
            toastClass: toastClass,
            onClick: onClickRedirect ? () => navigate(onClickRedirect) : null

        });
    };

    const handleWebSocketMessage = useCallback((data) => {
        console.log('Llego mensaje ws: ', data)
        if (data.type === "SMART_OUTLET") {
            const deviceId = data.id;
            const newStatus = !!data.status;
            setDevices(prev => {
                return prev.map(device => {
                    if (String(device.id) === String(deviceId)) {
                        return { ...device, status: newStatus };
                    }
                    return device;
                });
            });

        }
        else if (data.type === "DIMMER_UPDATE") {
            console.log("quiero updatear el brillo!")
            const deviceId = data.id;
            const brightness = parseInt(data.brightness);
            setDevices(prev => {
                return prev.map(device => {
                    if (String(device.id) === String(deviceId)) {
                        return { ...device, brightness: brightness };
                    }
                    console.log(device.brightness)
                    return device;
                });
            });
        }

        else if (data.type === "TEMP_UPDATE") {
            const deviceId = data.id;
            const temperature = parseFloat(data.temp);
            setDevices(prev => {
                return prev.map(device => {
                    if (String(device.id) === String(deviceId)) {
                        return { ...device, temperature };
                    }
                    return device;
                });
            });
        }
        else if (data.type === "OPENING_UPDATE") {
            const deviceId = data.id;
            console.log(data)
            const status = data.status;
            setDevices(prev => {
                return prev.map(device => {
                    if (String(device.id) === String(deviceId)) {
                        return { ...device, status };
                    }
                    return device;
                });
            });
        if(status){
            showNotification(
                "🚨 ¡Alarma activada!",
                "Se abrió una puerta sin autorización.",
                {
                    icon: warning,
                    duration: 5000,
                    toastClass: 'alarm-toast',
                    onClickRedirect: `/room/${id}`
                }
            );

            playAlarmSound();
        }

        }

    }, []);

    useEffect(() => {
        connectWebSocket(handleWebSocketMessage);
    }, [handleWebSocketMessage]);

    useEffect(() => {
        console.log(id)
        const fetchRoom = async () => {
            try {
                const room = await getRoomDetails(id, token);
                console.log(room)
                setRoomName(room.name || "Habitación sin nombre");
                setDevices(room.deviceList || []);
            } catch (err) {
                console.error(err);
                setError("Error al cargar detalles de la habitación");
            }
        };
        fetchRoom();
    }, [id, token]);

    const handleAddClick = async () => {
        setEditMode(false);
        setAddMode(true);
        try {
            const devices = await getUnconfiguredDevices(token);
            setAvailableDevices(devices || []);
        } catch (err) {
            console.error(err);
            setError("Error al cargar los dispositivos");
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

    if (addMode) {
        setHeaderTitle("Agregar dispositivo")
        return (
            <div className="main-container">
                <BackOrCloseButton type="arrow" onClick={() => setAddMode(false)} />
                <div className="room2-grid">
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
        setHeaderTitle("Editar dispositivo")
        return (
            <div className="main-container">
                <BackOrCloseButton type="arrow" onClick={() => setEditMode(false)} />
                <div className="room2-grid">
                    {devices.map((device, index) => (
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
                    ))}
                    <div className="add-device-icon">
                        <button onClick={handleAddClick}>
                            <FiPlus size={24} className="icon" />
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
    setHeaderTitle(roomName)
    return (
        <div className="main-container">
            <BackOrCloseButton/>
            {devices.length > 0 && (
                <div className="edit-container">
                <div className="edit-button">
                        <button onClick={() => setEditMode(true)}>
                            <FiEdit size={24}/>
                        </button>
                    </div>
                </div>
            )}

            <div className="room2-grid">
                {devices.length > 0 ? (
                    devices.map((device, index) => (
                        <div key={index}
                             className={`room-button ${device.status ? 'active' : ''} ${
                                 device.type === 'opening_sensor' && device.status ? 'alarm-active' : ''
                             }`}
                             onClick={() => handleDeviceClick(device)}>
                            <div
                                className={`device-icon ${device.type === 'opening_sensor' && device.status ? 'alarm-active' : ''}`}>
                                {getDeviceIcon(device.type)}</div>
                            <span>{device.name}</span>
                            {device.type === "temperature_sensor" && device.temperature !== null && (
                                <div className="device-info">
                                    <small>{device.temperature}°C</small>
                                </div>
                            )}
                            {device.type === "smart_outlet" && (
                                <div className="switch-container" onClick={(e) => e.stopPropagation()}>
                                    <label className="switch">
                                        <input
                                            type="checkbox"
                                            checked={!!device.status}
                                            onChange={() => toggleLight(device)}
                                        />
                                        <span className={`slider round ${device.status ? 'on' : 'off'}`}></span>
                                    </label>
                                </div>
                            )}

                            {device.type === "dimmer" && (
                                <div className="slider-container" onClick={(e) => e.stopPropagation()}>
                                    <label className="slider-label">
                                        <input
                                            type="range"
                                            min="0"
                                            max="100"
                                            value={device.brightness || 0} // Ajusta según el valor actual del brillo
                                            onChange={(e) => setBrightness(device, e.target.value)} // Envía el valor al backend
                                        />
                                        <span className="slider-value">{device.brightness || 0}%</span> {/* Muestra el valor */}
                                    </label>
                                </div>
                            )}


                            {device.type === 'opening_sensor' && device.status && (
                                <span className="alarm-status">ACTIVA</span>
                            )}
                        </div>
                    ))
                ) : (
                    <div className="no-devices">
                        <p>Aún no tenés dispositivos...</p>
                        <button onClick={handleAddClick}>
                            <FiPlus/> Agregar dispositivo
                        </button>
                    </div>
                )}
            </div>
            {toast && (
                <Toast
                    key={toast.key}
                    message={toast.message}
                    duration={3000}
                    onClose={() => setToast(null)}
                />
            )}
        </div>
    );
};

export default RoomDetail;
