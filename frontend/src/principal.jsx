import './principal.css';
import logo from '../public/NeoHub.png';
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
        { id: 2, name: "Temperatura", type: "thermostat", temp: 22 },
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
        if (type === 'light') {
            toggleLight(id);
        }
    };

    return (
        <div className="dashboard">
            {loading && <div className="loading-overlay">Enviando comando...</div>}
            {error && <div className="error-message">{error}</div>}

            <div className="logo-container">
                <img src={logo} alt="Smart Home Hub" className="logo" />
            </div>

            {devices.map((device, index) => {
                const angle = (index * 60) - 90;
                const radius = 180;
                const x = Math.cos(angle * Math.PI / 180) * radius;
                const y = Math.sin(angle * Math.PI / 180) * radius;

                return (
                    <div
                        key={device.id}
                        className={`device ${device.isOn ? 'active' : ''}`}
                        style={{
                            transform: `translate(calc(50% + ${x}px), calc(50% + ${y}px))`
                        }}
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
                    </div>
                );
            })}
        </div>
    );
};

export default SmartHomeDashboard;