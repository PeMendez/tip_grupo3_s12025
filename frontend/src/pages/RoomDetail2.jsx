import { useEffect, useState } from "react";
import {useNavigate, useParams} from "react-router-dom";
import useRoomData from "../hooks/useRoomData.js";
import useDeviceData from "../hooks/useDeviceData.js";
import {useTitle} from "../contexts/TitleContext.jsx";
import GridView from "../components/grid/GridView.jsx";
import {getDeviceIcon} from "../components/grid/utils.jsx";
import RoundButton from "../components/RoundButton.jsx";
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import Toast from "../components/Toast.jsx";

const RoomDetail2 = () => {
    const { id } = useParams();
    const navigate = useNavigate()
    const [mode, setMode] = useState('view'); // 'view' | 'add' | 'edit'
    const { setHeaderTitle } = useTitle();

    const {
        roomName,
        devices,
        availableDevices,
        fetchRoom,
        setDevices,
        fetchAvailableDevices
    } = useRoomData(id);

    const {
        toggleLight,
        handleAddDevice,
        handleDeleteDevice,
        toast,
        setToast
    } = useDeviceData(id, fetchRoom, setDevices, fetchAvailableDevices);

    useEffect(() => {
        const titles = {
            'view': roomName || "",
            'add': "Agregar Dispositivos",
            'edit': "Editar Dispositivos"
        };
        setHeaderTitle(titles[mode]);
    }, [mode, roomName, setHeaderTitle]);

    useEffect(() => {
        if (mode === 'add') {
            fetchAvailableDevices();
        }
    }, [mode, fetchAvailableDevices]);

    useEffect(() => {
        if (mode === 'edit') {
            fetchRoom();
        }
    }, [mode, fetchRoom]);

    const handleDeviceClick = (device) => {
        if (mode !== 'view') return;

        if (device.type === 'smart_outlet' || device.type === 'dimmer') {
            toggleLight(device.id);
        }
    };

    const handleClose = () => {
        navigate('/home');
    };

    if (mode === 'add') {
        return (
            <div>
                {availableDevices.length === 0 ? (
                    <p>Conecta un dispositivo!</p>
                ) : (
                    <GridView
                        type="device"
                        items={availableDevices}
                        onItemClick={(device) => {
                            handleAddDevice(device);
                            setMode('view');
                        }}
                        onClose={() => setMode('view')}
                        getIcon={(device) => getDeviceIcon(device.type)}
                    />
                )}
            </div>
        );
    }

    if (mode === 'edit') {
        return (
            <GridView
                type="device"
                items={devices}
                editMode={true}
                onDelete={handleDeleteDevice}
                onAdd={() => setMode('add')}
                onClose={() => setMode('view')}
                getIcon={(device) => getDeviceIcon(device.type)}
            />
        );
    }

    return (
        <div className="main-container">
            {devices.length > 0 ? (
                <>
                    <div className="edit-container">
                        <RoundButton type="edit" onClick={() => setMode('edit')}/>
                    </div>
                    <GridView
                        type="device"
                        items={devices}
                        onItemClick={handleDeviceClick}
                        getIcon={(device) => getDeviceIcon(device.type)}
                    />
                </>
            ) : (
                <div className="main-container">
                    <BackOrCloseButton type="arrow" onClick={handleClose}/>
                    <div className="no-rooms">
                        <p>Aún no tenés dispositivos...</p>
                        <RoundButton type="edit" onClick={() => setMode('add')}/>
                    </div>
                </div>
            )}

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

export default RoomDetail2;