import { FiEdit, FiPlus } from 'react-icons/fi';
import DeviceCard from './DeviceCard';
import BackOrCloseButton from "../BackOrCloseButton";

const DeviceGrid = ({ roomName, devices, onEdit, onAddDevice, toggleLight, setBrightness }) => {
    return (
        <>
            <div className="header-wrapper">
                <div className="header">
                    <BackOrCloseButton/>
                    <h2>{roomName}</h2>
                </div>
            </div>

            {devices.length > 0 && (
                <div className="edit-container">
                    <div className="edit-button">
                        <button onClick={onEdit}>
                            <FiEdit size={24}/>
                        </button>
                    </div>
                </div>
            )}

            <div className="room2-grid">
                {devices.length > 0 ? (
                    devices.map((device, index) => (
                        <DeviceCard
                            key={index}
                            device={device}
                            toggleLight={toggleLight}
                            setBrightness={setBrightness}
                        />
                    ))
                ) : (
                    <div className="no-devices">
                        <p>Aún no tenés dispositivos...</p>
                        <button onClick={onAddDevice}>
                            <FiPlus/> Agregar dispositivo
                        </button>
                    </div>
                )}
            </div>
        </>
    );
};

export default DeviceGrid;