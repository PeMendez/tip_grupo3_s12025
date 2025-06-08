import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import useRoomData from "../hooks/useRoomData.js";
import useDeviceData from "../hooks/useDeviceData.js";
import {useTitle} from "../contexts/TitleContext.jsx";
import GridView from "../components/grid/GridView.jsx";
import {getDeviceIcon} from "../components/grid/utils.jsx";
import RoundButton from "../components/RoundButton.jsx";
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import Toast from "../components/Toast.jsx";

const RoomDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate()
    const [mode, setMode] = useState('view'); // 'view' | 'add' | 'edit'
    const { setHeaderTitle } = useTitle();
    const [loading, setLoading] = useState(false);

    const {
        roomName,
        devices,
        availableDevices,
        fetchRoom,
        setDevices,
        fetchAvailableDevices,
        deviceAck
    } = useRoomData(id);

    const {
        toggleLight,
        handleAddDevice,
        handleDeleteDevice,
        toast,
        setToast,
        setBrightness
    } = useDeviceData(id, fetchRoom, setDevices, fetchAvailableDevices, deviceAck);

    useEffect(() => {
        const titles = {
            'view': roomName || "",
            'add': "Agregar Dispositivos",
            'edit': "Editar Dispositivos"
        };
        setHeaderTitle(titles[mode]);
    }, [mode, roomName, setHeaderTitle]);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            if (mode === 'add') {
                await fetchAvailableDevices();
            } else if (mode === 'edit' || mode === 'view') {
                await fetchRoom();
            }
            setLoading(false);
        };

        fetchData();
    }, [mode, fetchAvailableDevices, fetchRoom]);


    const handleDeviceClick = (device) => {
        if (mode !== 'view') return;

        if (device.type === 'smart_outlet' || device.type === 'dimmer') {
            toggleLight(device.id);
        }
    };

    const getAckForDevice = (deviceId) => {
        const found = deviceAck.find(item => item.deviceId === deviceId);
        return found ? found.status : true;
    };

    const handleClose = () => {
        navigate('/home');
    };

    if (loading) {
        return (
            <div className="main-container">
                <BackOrCloseButton type="arrow" onClick={handleClose} />
                <div className="no-rooms">
                    <p>Cargando dispositivos...</p>
                </div>
            </div>
        );
    }

    if (mode === 'add') {
        return (
            <div>
                {availableDevices.length === 0 ? (
                        <div className="main-container">
                            <BackOrCloseButton type="arrow" onClick={handleClose}/>
                            <div className="no-rooms">
                                <p>Conecta un dispositivo!</p>
                            </div>
                        </div>
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
                        addMode={true}
                        editMode={false}
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
                onItemClick={ (device) => {console.log("No se que va aca pero si no se lo pasas no borra device.")} }
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
                        items={devices.map(device => ({
                            ...device,
                            ackStatus: getAckForDevice(device.id)
                        }))}
                        onItemClick={handleDeviceClick}
                        getIcon={(device) => getDeviceIcon(device.type)}
                        toggleLight={toggleLight}
                        setBrightness={setBrightness}
                        onClose={handleClose}
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

export default RoomDetail;