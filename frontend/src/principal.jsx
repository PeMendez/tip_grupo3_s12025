import './principal.css';
import logo from './assets/NeoHub.png';
import {useState, useEffect, useCallback, useRef} from 'react';
import {
    FiSun,
    FiThermometer,
    FiShield,
    FiVideo,
    FiLock,
    FiZap
} from 'react-icons/fi';
import { controlLight } from './api/homeService.js';
import { connectWebSocket, disconnectWebSocket } from './websocket';
import Toast from './Toast';

const SmartHomeDashboard = () => {

    const [devices, setDevices] = useState([
        { id: 1, name: "Luces", type: "light", isOn: false },
        { id: 2, name: "Temperatura", type: "thermostat", temp: 22, showTemp: false },
        { id: 3, name: "Alarma", type: "alarm", isActive: false }, // Estado de alarma aquí
        { id: 4, name: "Cámaras", type: "camera", isRecording: false },
        { id: 5, name: "Cerraduras", type: "lock", isLocked: true },
        { id: 6, name: "Energía", type: "plug", isOn: false },
    ]);

    const devicesRef = useRef(devices);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [toast, setToast] = useState(null);
    const audioContextRef = useRef(null);
    const [audioEnabled, setAudioEnabled] = useState(false);

    const initAudio = () => {
        if (!audioContextRef.current) {
            audioContextRef.current = new (window.AudioContext || window.webkitAudioContext)();
            setAudioEnabled(true);
        }
    };

    const isAlarmActive = devices.find(device => device.type === 'alarm')?.isActive || false;

    const toggleLight = async (id, forceState = null) => {
        setLoading(true);
        setError(null);

        try {
            const device = devices.find(d => d.id === id);
            const newState = forceState !== null ? forceState : !device.isOn;

            await controlLight(newState);

            setDevices(devices.map(device =>
                device.id === id ? { ...device, isOn: newState } : device
            ));
        } catch (err) {
            setError('Error al controlar las luces');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleDeviceClick = (id, type) => {
        if (type === 'thermostat') {
            setDevices(devices.map(device =>
                device.id === id
                    ? { ...device, showTemp: !device.showTemp }
                    : device
            ));
        } else if (type !== 'light' && type !== 'alarm') {
            alert(`Control para ${type} aún no implementado`);
        }
    };
    const playAlarmSound = useCallback(() => {
        if (!audioEnabled || !audioContextRef.current) return;

        try {
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

        } catch (e) {
            console.error("Error al crear audio:", e);
        }
    }, [audioEnabled]);

    const handleWebSocketMessage = useCallback((data) => {
        if (data.type === "ALARM_TRIGGERED") {

            setDevices(prev => {
                const updated = prev.map(device =>
                    device.type === 'alarm' ? { ...device, isActive: true } : device
                );

                const lightDevice = updated.find(d => d.type === 'light');
                if (lightDevice && !lightDevice.isOn) {
                    controlLight(true).then(() => {
                        setDevices(updated.map(device =>
                            device.type === 'light' ? { ...device, isOn: true } : device
                        ));
                    });
                }

                return updated;
            });
            setToast({
                message: data.message,
                key: Date.now()
            });

            playAlarmSound();


            if (Notification.permission === "granted") {
                new Notification("🚨 ¡Alarma activada!", {
                    body: data.message || "Se detectó un evento de alarma.",
                    icon: "https://cdn-icons-png.flaticon.com/512/484/484167.png",
                });
            } else if (Notification.permission !== "denied") {
                Notification.requestPermission().then(permission => {
                    if (permission === "granted") {
                        new Notification("🚨 ¡Alarma activada!", {
                            body: data.message || "Se detectó un evento de alarma.",
                            icon: "https://cdn-icons-png.flaticon.com/512/484/484167.png",
                        });
                    }
                });
            }

            /*setTimeout(() => {
                setDevices(prev => prev.map(device =>
                    device.type === 'alarm' ? { ...device, isActive: false } : device
                ));
            }, 5000);*/
        }

        if (data.type === "TEMP_UPDATE") {
            setDevices(prev => prev.map(device =>
                device.type === 'thermostat' ? { ...device, temp: data.temp } : device
            ));
        }

    }, [playAlarmSound]);

    useEffect(() => {
        devicesRef.current = devices;
    }, [devices]);

    useEffect(() => {
        connectWebSocket(handleWebSocketMessage);
        return () => disconnectWebSocket();
    }, [handleWebSocketMessage]);

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

    useEffect(() => {
        const handleClick = (event) => {
            if (event.target.tagName === 'INPUT' ||
                event.target.tagName === 'LABEL' ||
                event.target.closest('.switch')) {
                return;
            }
            setDevices(prev => prev.map(device =>
                device.type === 'alarm' ? { ...device, isActive: false } : device
            ));
        };

        if (isAlarmActive) {
            document.addEventListener('click', handleClick);
        }

        return () => {
            document.removeEventListener('click', handleClick);
        };
    }, [isAlarmActive]);

    return (
        <div className="dashboard-container compact-view">
            {loading && <div className="loading-overlay">Enviando comando...</div>}
            {error && <div className="error-message">{error}</div>}

            <div className="logo-header">
                <img src={logo} alt="Smart Home Hub" className="logo" />
            </div>

            <div className="devices-grid">
                {devices.map((device) => (
                    <div
                        key={device.id}
                        className={`device-card ${device.isOn ? 'active' : ''} ${
                            device.type === 'alarm' && isAlarmActive ? 'alarm-active' : ''
                        }`}
                        onClick={() => handleDeviceClick(device.id, device.type)}
                    >
                        <div className="device-icon">
                            {device.type === 'light' && <FiSun />}
                            {device.type === 'thermostat' && <FiThermometer />}
                            {device.type === 'alarm' && <FiShield />}
                            {device.type === 'camera' && <FiVideo />}
                            {device.type === 'lock' && <FiLock />}
                            {device.type === 'plug' && <FiZap />}
                        </div>
                        <span className="device-label">{device.name}</span>

                        {device.type === 'light' && (
                            <label className="switch">
                                <input
                                    type="checkbox"
                                    checked={device.isOn}
                                    onChange={(e) => {
                                        e.stopPropagation();
                                        toggleLight(device.id);
                                    }}
                                />
                                <span className="slider round"></span>
                            </label>
                        )}

                        {device.type === 'thermostat' && device.showTemp && (
                            <div className="temperature-display">
                                <span className="device-value">{device.temp}°C</span>
                            </div>
                        )}

                        {device.type === 'alarm' && isAlarmActive && (
                            <span className="alarm-status">ACTIVA</span>
                        )}
                    </div>
                ))}
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

export default SmartHomeDashboard;