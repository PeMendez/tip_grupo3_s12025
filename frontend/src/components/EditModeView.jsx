import BackOrCloseButton from "../components/BackOrCloseButton";
import DeleteModal from "./DeleteModal";
import { FiPlus } from 'react-icons/fi';
import { getDeviceIcon } from "./utils.jsx";

const EditModeView = ({ devices, onDeleteDevice, onAddDevice, onClose }) => {
    const [deviceToDelete, setDeviceToDelete] = useState(null);

    return (
        <div className="main-container">
            <div className="header-wrapper">
                <div className="header">
                    <BackOrCloseButton type="arrow" onClick={onClose} />
                    <h2>Editar Dispositivos</h2>
                </div>
            </div>

            <div className="room2-grid">
                {devices.map((device, index) => (
                    <div
                        key={index}
                        className="room-editable-container"
                        onClick={() => setDeviceToDelete(device)}
                    >
                        <div className="room-button edit-mode">
                            <div className="device-icon">{getDeviceIcon(device.type)}</div>
                            <span>{device.name}</span>
                            <div className="delete-icon-full">🗑️</div>
                        </div>
                    </div>
                ))}

                <div className="add-device-icon">
                    <button onClick={onAddDevice}>
                        <FiPlus size={24} className="icon" />
                    </button>
                </div>
            </div>

            {deviceToDelete && (
                <DeleteModal
                    device={deviceToDelete}
                    onConfirm={() => {
                        onDeleteDevice(deviceToDelete.id);
                        setDeviceToDelete(null);
                    }}
                    onCancel={() => setDeviceToDelete(null)}
                />
            )}
        </div>
    );
};

export default EditModeView;