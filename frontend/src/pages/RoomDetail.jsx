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
        setDevices,
        availableDevices,
        fetchRoomEdit,
        fetchAvailableDevices,
        deviceAck,
        fetchRoomRole
    } = useRoomData(id);

    const {
        toggleLight,
        setBrightness,
        handleAddDevice,
        handleDeleteDevice,
        handleFactoryReset,
        toast,
        setToast,
    } = useDeviceData(id, fetchRoomRole, setDevices);

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
            } else if (mode === 'edit') {
                await fetchRoomEdit();
            } else if (mode === 'view') {
                await fetchRoomRole();
            }
            setLoading(false);
        };

        fetchData();
    }, [mode, fetchAvailableDevices, fetchRoomRole, fetchRoomEdit]);

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
            <div className="main-container">
                {availableDevices.length === 0 ? (
                        <div className="main-container">
                            <BackOrCloseButton type="arrow" onClick={handleClose}/>
                            <div className="no-rooms">
                                <p>Conecta un dispositivo!</p>
                            </div>
                        </div>
                ) : (
                    <div className="main-container">
                        <BackOrCloseButton type="arrow" onClose={() => setMode('view')}/>
                    <GridView
                        type="device"
                        items={availableDevices}
                        onItemClick={(device) => {
                            handleAddDevice(device, {
                                onStart: () => setLoading(true),
                                onEnd: () => {
                                    setLoading(false);
                                    setMode('view');
                                }
                            });
                        }}
                        getIcon={(device) => getDeviceIcon(device.type)}
                        addMode={true}
                        editMode={false}
                    />
                    </div>
                )}
            </div>
        );
    }

    if (mode === 'edit') {
        return (
            <div className="main-container">
                <BackOrCloseButton type="arrow" onClick={() => setMode('view')}/>
                <GridView
                    type="device"
                    items={devices}
                    editMode={true}
                    onDelete={handleDeleteDevice}
                    onResetFactory={handleFactoryReset}
                    onItemClick={(device) => {
                        console.log("No se que va aca pero si no se lo pasas no borra device.")
                    }}
                    onAdd={() => setMode('add')}
                    getIcon={(device) => getDeviceIcon(device.type)}
                />

            </div>
                );
                }

                return (
                <div className="main-container">
                    <BackOrCloseButton type="arrow" onClick={handleClose}/>
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