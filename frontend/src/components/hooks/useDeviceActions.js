import {useState, useCallback, useRef, useEffect} from "react";
import { useNavigate } from "react-router-dom";
import {
    smartOutletCommand,
    dimmerCommand
} from "../../api/deviceService";
import {
    deleteDevice,
    addDeviceToRoom } from "../../api/roomService.js"
import { connectWebSocket } from "../../websocket";
import warning from '../../assets/warning.png';

const useDeviceActions = (roomId, fetchRoom, setDevices) => {
    const [toast, setToast] = useState(null);
    const token = localStorage.getItem('token');
    const audioContextRef = useRef(null);
    const navigate = useNavigate();

    const playAlarmSound = useCallback(() => {
        if (!audioContextRef.current) {
            audioContextRef.current = new (window.AudioContext || window.webkitAudioContext)();
        }

        const audioContext = audioContextRef.current;
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();

        oscillator.type = "sine";
        oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        gainNode.gain.exponentialRampToValueAtTime(0.3, audioContext.currentTime + 0.1);
        gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 0.2);

        oscillator.start();

        setTimeout(() => {
            oscillator.stop();
        }, 5000);
    }, []);

    const showNotification = useCallback((title, message, options = {}) => {
        const {
            icon = "https://cdn-icons-png.flaticon.com/512/619/619153.png",
            duration = 3000,
            toastClass = '',
            onClickRedirect = null
        } = options;

        if (Notification.permission === "granted") {
            const notification = new Notification(title, {
                body: message,
                icon: icon
            });

            if (onClickRedirect) {
                notification.onclick = () => {
                    navigate(onClickRedirect);
                    window.focus();
                };
            }
        } else if (Notification.permission !== "denied") {
            Notification.requestPermission().then(permission => {
                if (permission === "granted") {
                    new Notification(title, {
                        body: message,
                        icon: icon
                    });
                }
            });
        }

        setToast({
            message: message,
            key: Date.now(),
            duration: duration,
            toastClass: toastClass,
            onClick: onClickRedirect ? () => navigate(onClickRedirect) : null
        });
    }, [navigate]);

    const handleWebSocketMessage = useCallback((data) => {
        console.log('Llego mensaje ws: ', data);

        if (data.type === "SMART_OUTLET") {
            const deviceId = data.id;
            const newStatus = !!data.status;
            setDevices(prev => prev.map(device =>
                String(device.id) === String(deviceId) ? { ...device, status: newStatus } : device
            ));
        }
        else if (data.type === "DIMMER_UPDATE") {
            const deviceId = data.id;
            const brightness = parseInt(data.brightness);
            setDevices(prev => prev.map(device =>
                String(device.id) === String(deviceId) ? { ...device, brightness } : device
            ));
        }
        else if (data.type === "TEMP_UPDATE") {
            const deviceId = data.id;
            const temperature = parseFloat(data.temp);
            setDevices(prev => prev.map(device =>
                String(device.id) === String(deviceId) ? { ...device, temperature } : device
            ));
        }
        else if (data.type === "OPENING_UPDATE") {
            const deviceId = data.id;
            const status = data.status;
            setDevices(prev => prev.map(device =>
                String(device.id) === String(deviceId) ? { ...device, status } : device
            ));

            if(status){
                showNotification(
                    "🚨 ¡Alarma activada!",
                    "Se abrió una puerta sin autorización.",
                    {
                        icon: warning,
                        duration: 5000,
                        toastClass: 'alarm-toast',
                        onClickRedirect: `/room/${roomId}`
                    }
                );
                playAlarmSound();
            }
        }
    }, [roomId, showNotification, playAlarmSound, setDevices]);

    useEffect(() => {
        connectWebSocket(handleWebSocketMessage);
    }, [handleWebSocketMessage]);

    const toggleLight = useCallback(async (device) => {
        try {
            console.log(device, token)
            await smartOutletCommand(device, !device.status, token);
        } catch (error) {
            console.error("Error completo:", {
                message: error.message,
                status: error.response?.status,
                data: error.response?.data,
                headers: error.response?.headers
            });
            console.error(error);
        }
    }, [token]);

    const setBrightness = useCallback(async (device, brightness) => {
        try {
            await dimmerCommand(device, brightness, token);
        } catch (err) {
            console.error(err);
        }
    }, [token]);

    const handleAddDevice = useCallback(async (device) => {
        try {
            await addDeviceToRoom(roomId, device.id, token);
            await fetchRoom();
        } catch (err) {
            console.error("Error al agregar dispositivo", err);
        }
    }, [roomId, token, fetchRoom]);

    const handleDeleteDevice = useCallback(async (deviceId) => {
        try {
            await deleteDevice(roomId, deviceId, token);
            await fetchRoom();
        } catch (err) {
            console.error("Error al eliminar dispositivo", err);
        }
    }, [roomId, token, fetchRoom]);

    return {
        toggleLight,
        setBrightness,
        handleAddDevice,
        handleDeleteDevice,
        showNotification,
        toast,
        setToast,
        playAlarmSound
    };
};

export default useDeviceActions;