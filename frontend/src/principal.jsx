import './principal.css';
import logo from './assets/NeoHub.png';
import { useState } from 'react';
import {
    FiSun,
    FiThermometer,
    FiShield,
    FiVideo,
    FiLock,
    FiZap
} from 'react-icons/fi';
import { controlLight } from './api/homeService.js';

const SmartHomeDashboard = () => {
    const [devices, setDevices] = useState([
        { id: 1, name: "Luces", type: "light", isOn: false },
        { id: 2, name: "Temperatura", type: "thermostat", temp: 22, showTemp: false },
        { id: 3, name: "Alarma", type: "alarm", isActive: false },
        { id: 4, name: "Cámaras", type: "camera", isRecording: false },
        { id: 5, name: "Cerraduras", type: "lock", isLocked: true },
        { id: 6, name: "Energía", type: "plug", isOn: false },
    ]);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const toggleLight = async (id) => {
        setLoading(true);
        setError(null);

        try {
            const device = devices.find(d => d.id === id);
            const newState = !device.isOn;

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

        } else if (type !== 'light') {
            alert(`Control para ${type} aún no implementado`);
        }
    };

    return (
        <div className="dashboard-container">
            {loading && <div className="loading-overlay">Enviando comando...</div>}
            {error && <div className="error-message">{error}</div>}

            <div className="logo-header">
                <img src={logo} alt="Smart Home Hub" className="logo" />
            </div>

            <div className="devices-grid">
                {devices.map((device) => (
                    <div
                        key={device.id}
                        className={`device-card ${device.isOn ? 'active' : ''}`}
                        onClick={() => handleDeviceClick(device.id, device.type)}
                    >
                        <div className="device-icon">
                            {device.type === 'light' && <FiSun/>}
                            {device.type === 'thermostat' && <FiThermometer/>}
                            {device.type === 'alarm' && <FiShield/>}
                            {device.type === 'camera' && <FiVideo/>}
                            {device.type === 'lock' && <FiLock/>}
                            {device.type === 'plug' && <FiZap/>}
                        </div>
                        <span className="device-label">{device.name}</span>

                        <div className="device-extra-content">
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
                                    <div className="temperature-loading">Actualizando...</div>
                                </div>
                            )}
                        </div>

                    </div>
                ))}
            </div>
        </div>
    );
};

export default SmartHomeDashboard;