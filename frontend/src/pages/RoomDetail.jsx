import { useState } from "react";
import { useParams } from "react-router-dom";
import { FiSun, FiThermometer, FiShield, FiVideo, FiLock } from 'react-icons/fi';
import { LuAirVent } from "react-icons/lu";
import './roomDetail.css';

const RoomDetail = () => {
    const { nameroom } = useParams();
    const [dispositivos, setDispositivos] = useState([]);
    const [showDeviceOptions, setShowDeviceOptions] = useState(false);
    const deviceOptions = [
        { name: 'Luz', icon: <FiSun size={24} /> },
        { name: 'Temperatura', icon: <FiThermometer size={24} /> },
        { name: 'Alarma', icon: <FiShield size={24} /> },
        { name: 'Aire Acondicionado', icon: <LuAirVent size={24} /> },
        { name: 'Control TV', icon: <FiVideo size={24} /> },
        { name: 'Enchufe', icon: <FiLock size={24} /> },
    ];

    const handleAddDevice = (device) => {
        setDispositivos([...dispositivos, device]);
        setShowDeviceOptions(false);
    };

    return (
        <div className="room-detail-container">
            <h2>{nameroom}</h2>
            {dispositivos.length === 0 ? (
                <div className="no-devices">
                    <p>Aún no tenés dispositivos en esta habitación.</p>
                    <button className="add-device-btn" onClick={() => setShowDeviceOptions(true)}>Agregar dispositivo</button>
                </div>
            ) : (
                <div className="device-grid">
                    {dispositivos.map((device, index) => (
                        <div key={index} className="device-card">
                            <div className="device-icon">{device.icon}</div>
                            <div className="device-name">{device.name}</div>
                        </div>
                    ))}
                </div>
            )}

            {showDeviceOptions && (
                <div className="device-options">
                    <h3>Selecciona un dispositivo</h3>
                    <div className="device-options-grid">
                        {deviceOptions.map((device, index) => (
                            <button
                                key={index}
                                className="device-option-btn"
                                onClick={() => handleAddDevice(device)}
                            >
                                <div className="device-icon">{device.icon}</div>
                                <div className="device-name">{device.name}</div>
                            </button>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default RoomDetail;
