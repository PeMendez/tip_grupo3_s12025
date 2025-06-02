import { useState } from "react";
import { useParams } from "react-router-dom";
import useDeviceActions  from "../components/hooks/useDeviceActions.js";
import useRoomData from "../components/hooks/useRoomData.js"
import DeviceGrid from "../components/DeviceGrid";
import EditModeView from "../components/EditModeView";
import AddModeView from "../components/AddModeView";
import Toast from '../Toast';
import './roomDetail.css';

const RoomDetail = () => {
    const { id } = useParams();
    const {
        roomName,
        devices,
        availableDevices,
        error,
        fetchRoom,
        setDevices
    } = useRoomData(id);

    const {
        toggleLight,
        setBrightness,
        handleAddDevice,
        handleDeleteDevice,
        showNotification,
        toast,
        setToast
    } = useDeviceActions(id, fetchRoom, setDevices);

    const [editMode, setEditMode] = useState(false);
    const [addMode, setAddMode] = useState(false);


    if (addMode) {
        return (
            <AddModeView
                availableDevices={availableDevices}
                onAddDevice={handleAddDevice}
                onClose={() => setAddMode(false)}
            />
        );
    }

    if (editMode) {
        return (
            <EditModeView
                devices={devices}
                deleteDevice={handleDeleteDevice}
                onAddDevice={() => setAddMode(true)}
                onClose={() => setEditMode(false)}
                showNotification={showNotification}
            />
        );
    }

    return (
        <div className="main-container">
            <DeviceGrid
                roomName={roomName}
                devices={devices}
                onEdit={() => setEditMode(true)}
                onAddDevice={() => setAddMode(true)}
                toggleLight={toggleLight}
                setBrightness={setBrightness}
            />

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

export default RoomDetail;