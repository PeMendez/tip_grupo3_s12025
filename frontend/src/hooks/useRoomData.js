import { useState, useEffect, useCallback } from "react";
import { getRoomDetails } from "../api/roomService";
import {getUnconfiguredDevices } from "../api/deviceService.js"

const useRoomData = (roomId) => {
    const [roomName, setRoomName] = useState("");
    const [devices, setDevices] = useState([]);
    const [availableDevices, setAvailableDevices] = useState([]);
    const [deviceAck, setDeviceAck] = useState([])
    const [error, setError] = useState(null);
    const token = localStorage.getItem('token');

    const fetchRoom = useCallback(async () => {
        try {
            const {room, ack} = await getRoomDetails(roomId, token);
            setRoomName(room.name || "Habitación sin nombre");
            setDevices(room.deviceList || []);
            setDeviceAck(
                ack ? Object.entries(ack).map(([deviceId, status]) => ({
                    deviceId: parseInt(deviceId),
                    status
                })) : []
            );
        } catch (err) {
            console.error(err);
            setError("Error al cargar detalles de la habitación");
        }
    }, [roomId, token]);

    const fetchAvailableDevices = useCallback(async () => {
        try {
            const devices = await getUnconfiguredDevices(token);
            setAvailableDevices(devices || []);
        } catch (err) {
            console.error(err);
            setError("Error al cargar los dispositivos");
        }
    }, [token]);

    useEffect(() => {
        fetchRoom();
    }, [fetchRoom]);

    useEffect(() => {
        fetchAvailableDevices();
    }, [fetchAvailableDevices]);

    return {
        roomName,
        devices,
        availableDevices,
        error,
        fetchRoom,
        fetchAvailableDevices,
        setDevices,
        deviceAck
    };
};

export default useRoomData;