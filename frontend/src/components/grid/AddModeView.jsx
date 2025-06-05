import BackOrCloseButton from "../BackOrCloseButton.jsx";
import { getDeviceIcon } from "./utils.jsx";

const AddModeView = ({ availableDevices, onAddDevice, onClose }) => {
    return (
        <div className="main-container">
            <div className="header-wrapper">
                <div className="header">
                    <BackOrCloseButton type="arrow" onClick={onClose} />
                    <h2>Agregar Dispositivos</h2>
                </div>
            </div>

            <div className="room2-grid">
                {availableDevices.map((device, index) => (
                    <button
                        key={index}
                        onClick={() => onAddDevice(device)}
                        className="room-button"
                    >
                        <div className="device-icon">{getDeviceIcon(device.type)}</div>
                        <span>{device.name}</span>
                    </button>
                ))}
            </div>
        </div>
    );
};

export default AddModeView;