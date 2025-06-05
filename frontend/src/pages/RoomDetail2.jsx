import {useEffect, useState} from "react";
import { useParams } from "react-router-dom";
import useRoomData from "../hooks/useRoomData.js"
import DefaultModeView from "../components/grid/DefaultModeView.jsx";
import EditModeView from "../components/grid/EditModeView";
import AddModeView from "../components/grid/AddModeView";
import Toast from '../components/Toast.jsx';
import './styles/roomDetail.css';
import useDeviceData from "../hooks/useDeviceData.js";
import {useTitle} from "../contexts/TitleContext.jsx";

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
    } = useDeviceData(id, fetchRoom, setDevices);

    const [editMode, setEditMode] = useState(false);
    const [addMode, setAddMode] = useState(false);

    const {setHeaderTitle} = useTitle();

    useEffect(() => {
        setHeaderTitle("");
    }, []);

    useEffect(() => {
        if (addMode) {
            setHeaderTitle("Agregar Dispositivos");
        }
    }, [addMode]);

    useEffect(() => {
        if (editMode) {
            setHeaderTitle("Editar Mis Dispositivos");
        }
    }, [editMode]);


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
            <DefaultModeView
                roomName={roomName}
                devices={devices}
                onEdit={() => setEditMode(true)}
                onAddDevice={() => setAddMode(true)}
                toggleLight={toggleLight}
                setBrightness={setBrightness}
                setHeaderTitle={setHeaderTitle}
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