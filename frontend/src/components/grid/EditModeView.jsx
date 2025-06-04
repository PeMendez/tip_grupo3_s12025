import {useState}  from "react";
import BackOrCloseButton from "../BackOrCloseButton";
import DeleteModal from "../DeleteModal";
import { FiPlus } from 'react-icons/fi';
import { getDeviceIcon } from "./utils.jsx";


const EditModeView = ({ devices, deleteDevice, onAddDevice, onClose, showNotification  }) => {
    const [deviceToDelete, setDeviceToDelete] = useState(null);

    const handleConfirmDelete = async () => {
        if (!deviceToDelete) return;

        try {
            await deleteDevice(deviceToDelete.id);
            showNotification(
                "Dispositivo eliminado",
                `Se eliminó ${deviceToDelete.name} correctamente`,
                { duration: 3000 }
            );
        } catch (error) {
            showNotification(
                "Error",
                `No se pudo eliminar ${deviceToDelete.name}`,
                { duration: 5000, toastClass: 'error-toast' }
            );
            console.error("Error al eliminar:", error);
        } finally {
            setDeviceToDelete(null);
        }
    };

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
                    onConfirm={handleConfirmDelete}
                    onCancel={() => setDeviceToDelete(null)}
                />
            )}
        </div>
    );
};

export default EditModeView;