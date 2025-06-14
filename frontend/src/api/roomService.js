import axios from 'axios';

const API_URL = 'http://localhost:8080/room';

export const getRoomDetails = async (roomId, token) => {
    try {
        const response = await axios.get(`${API_URL}/${roomId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const ackResponse = await axios.get(`${API_URL}/${roomId}/ack`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        console.log(ackResponse)
        return {
            room: response.data,
            ack: ackResponse.data.data
        };
    } catch (error) {
        console.error('Error al obtener detalles de la habitación:', error);
        throw error;
    }
};

export const getRoomDetailsEdit = async (roomId, token) => {
    try {
        const response = await axios.get(`${API_URL}/${roomId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data

    } catch (error) {
        console.error('Error al obtener detalles de la habitación:', error);
        throw error;
    }
};

export const addDeviceToRoom = async (roomId, deviceId, token) => {
    try {
        const response = await axios.post(`${API_URL}/${roomId}/addDevice/${deviceId}`,
            {
                roomId: roomId,
                deviceId: deviceId
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
        console.error('Error al agregar dispositivo:', error);
        throw error;
    }
};

export const deleteDevice = async (roomId, deviceId, token) => {
    try {
        const response = await axios.post(`${API_URL}/${roomId}/removeDevice/${deviceId}`,
            {
                roomId: roomId,
                deviceId: deviceId
            },{
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );
        return response.data;
    } catch (error) {
        console.error('Error deleting device:', error);
        throw error;
    }
};

export const factoryResetDevice = async (roomId, deviceId, token) => {
    try {
        const response = await axios.delete(`${API_URL}/${roomId}/resetDevice/${deviceId}`,
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );
        return response.data;
    } catch (error) {
        console.error('Error reseteando el dispositivo:', error);
        throw error;
    }
};

export const getRoomDetailsForRole = async (roomId, token) => {
    try {
        const response = await axios.get(`${API_URL}/devices/${roomId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const ackResponse = await axios.get(`${API_URL}/${roomId}/ack`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        console.log(ackResponse)
        return {
            room: response.data,
            ack: ackResponse.data.data
        };
    } catch (error) {
        console.error('Error al obtener detalles de la habitación:', error);
        throw error;
    }
};