import BackOrCloseButton from "../BackOrCloseButton.jsx";
import { getDeviceIcon } from "./utils.jsx";

const AddModeView = ({ availableDevices, onAddDevice, onClose }) => {
    return (
        <div className="main-container">
            <div className="header-wrapper">
                <div className="header">
                    <BackOrCloseButton type="arrow" onClick={onClose} />
                </div>
            </div>

            {availableDevices.length === 0 ? (
                <p>Conecta un dispositivo!</p>
            ) : (
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
                </div> )
            }
        </div>
    );
};

export default AddModeView;