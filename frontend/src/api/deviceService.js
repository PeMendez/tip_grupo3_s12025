import axios from 'axios';

const API_URL = 'http://localhost:8080/devices';

export const getUnconfiguredDevices = async (token) => {
    try {
        const response = await axios.get(`${API_URL}/unconfigured`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error al obtener los dispositivos desconfigurados:', error);
        throw error;
    }
};

export const getConfiguredDeviceCount = async (token) => {
    try {
        const response = await axios.get(`${API_URL}/configuredCount`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
        return response.data;
    } catch (error) {
        console.error('Error al obtener el conteo de dispositivos configurados:', error);
        throw new Error('No se pudo obtener el conteo de dispositivos configurados.');
    }
};

export const getDevice = async (deviceId, token) => {
    try {
        const response = await axios.get(`${API_URL}/${deviceId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error al obtener los dispositivos desconfigurados:', error);
        throw error;
    }
};

export const smartOutletCommand = async (device, forceState, token) => {
    const message = forceState? "turn_on" : "turn_off"
    console.log("Enviando un ", message)
    try {
        const response = await axios.post(`${API_URL}/message/${device.id}`,
            {
                message: message,
                parameter: ""
            },
            {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                }
            }
        );
        return response.data;
    } catch (error) {
        console.error('Error al controlar enchufe:', error);
        throw error;
    }
};

export const dimmerCommand = async (device, brightness, token) => {
    const message = "set_brightness"
    console.log("Enviando un ", message)
    try {
        const response = await axios.post(`${API_URL}/message/${device.id}`,
            {
                message: message,
                parameter: brightness.toString()
            },
            {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                }
            }
        );
        return response.data;
    } catch (error) {
        console.error('Error al controlar brillo:', error);
        throw error;
    }
};

export const getAllDevices = async (token) => {
    try {
        const response = await axios.get(`${API_URL}/devices`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
        return response.data;
    } catch (error) {
        console.error('Error al obtener los dispositivos:', error);
        throw new Error('No se pudieron obtener los dispositivos.');
    }
};

export const getAllDevicesConfigured = async (token) => {
    try {
        const response = await axios.get(`${API_URL}/configured`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
        return response.data;
    } catch (e) {
        console.log("Error al obtener los dispositivos configurados:", e);
        throw new Error("No se pudieron obtener los dispositivos configurados");
    }
}