import './principal.css';
import logo from './assets/NeoHub.png';
import snow from './assets/snowflake.png';
import warning from './assets/warning.png'
import {useState, useEffect, useCallback, useRef} from 'react';
import {
    FiSun,
    FiThermometer,
    FiShield,
    FiVideo,
    FiLock
} from 'react-icons/fi';
import { LuAirVent } from "react-icons/lu";
import { controlLight } from './api/homeService.js';
import { connectWebSocket, disconnectWebSocket } from './websocket';
import Toast from './Toast';

const SmartHomeDashboard = () => {

    const [devices, setDevices] = useState([
        { id: 1, name: "Luces", type: "light", isOn: false },
        { id: 2, name: "Temperatura", type: "thermostat", temp: 22, showTemp: false },
        { id: 3, name: "Alarma", type: "alarm", isActive: false },
        { id: 4, name: "Cámaras", type: "camera", isRecording: false },
        { id: 5, name: "Cerraduras", type: "lock", isLocked: true },
        { id: 6, name: "Aire acondicionado", type: "ac", isOn: false, mode: 'off'},
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

    const showNotification = (title, message, options = {}) => {
        const {
            icon = "https://cdn-icons-png.flaticon.com/512/619/619153.png",
            duration = 3000,
            toastClass = ''
        } = options;

        if (Notification.permission === "granted") {
            new Notification(title, {
                body: message,
                icon: icon
            });
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
            toastClass: toastClass
        });
    };

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

            showNotification(
                "🚨 ¡Alarma activada!",
                data.message || "Se abrió una puerta sin autorización.",
                {
                    icon: warning,
                    duration: 5000,
                    toastClass: 'alarm-toast',

                }
            );

            playAlarmSound();
        }

        if (data.type === "TEMP_UPDATE") {
            const temp = parseFloat(data.temp);
            const prevTemp = devices.find(d => d.type === 'thermostat')?.temp;

            if (temp !== prevTemp && (temp <= 14 || temp >= 28)) {
                const notificationOptions = {
                    title: temp >= 28 ? "¡Hace mucho calor!" : "Esto parece el polo sur",
                    message: temp >= 28 ? "Vamos a enfriar el ambiente." : "Vamos a calentar el ambiente.",
                    icon: temp >= 28 ? "https://cdn-icons-png.flaticon.com/512/599/599502.png" : snow,
                    toastClass: temp >= 28 ? 'cool-toast' : 'heat-toast'
                };
                showNotification(notificationOptions.title, notificationOptions.message, notificationOptions);
            }
            setDevices(prev => prev.map(device => {
                if (device.type === 'thermostat') return { ...device, temp, showTemp: true };
                if (device.type === 'ac') {
                    const mode = temp >= 28 ? 'cool' : temp <= 14 ? 'heat' : 'off';
                    return { ...device, isOn: mode !== 'off', mode };
                }
                return device;
            }));
        }

    }, [playAlarmSound, devices]);


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

    useEffect(() => {
        if (devices.some(d => d.type === 'thermostat' && d.showTemp)) {
            const timer = setTimeout(() => {
                setDevices(prev => prev.map(device =>
                    device.type === 'thermostat' ? { ...device, showTemp: false } : device
                ));
            }, 10000);

            return () => clearTimeout(timer);
        }
    }, [devices]);

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
                            {device.type === 'light' && <FiSun/>}
                            {device.type === 'thermostat' && <FiThermometer/>}
                            {device.type === 'alarm' && <FiShield/>}
                            {device.type === 'camera' && <FiVideo/>}
                            {device.type === 'lock' && <FiLock/>}
                            {device.type === 'ac' && (
                                <LuAirVent className={device.mode !== 'off' ? `ac-icon-${device.mode}` : ''}/>
                            )}
                        </div>
                        <span className={`device-label ${
                            device.type === 'ac' && device.mode !== 'off' ? `mode-${device.mode}` : ''}`}>
                            {device.name} </span>

                        {device.type === 'light' && (
                            <label className="switch">
                                <input
                                    type="checkbox"
                                    checked={device.isOn}
                                    onChange={async (e) => {
                                        e.stopPropagation();
                                        try {
                                            await toggleLight(device.id);
                                        } catch (err) {
                                            console.error(err);
                                        }
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

                        {device.type === 'ac' && device.mode !== 'off' && (
                            <span className={`ac-mode mode-${device.mode}`}></span>
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