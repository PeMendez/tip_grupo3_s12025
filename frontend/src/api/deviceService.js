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

export const controlLight = async (device, forceState, token) => {
    const message = forceState? "turn_on" : "turn_off"
    console.log("Enviando un ", message)
    try {
        const response = await axios.post(`${API_URL}/message/${device.id}`,
            {
                message: message
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
        console.error('Error al controlar luces:', error);
        throw error;
    }
};
