import axios from 'axios';

const API_URL = 'http://localhost:8080/room';

export const getRoomDetails = async (roomId, token) => {
    try {
        const response = await axios.get(`${API_URL}/${roomId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error al obtener detalles de la habitación:', error);
        throw error;
    }
};

export const addDeviceToRoom = async (roomId, device, token) => {
    try {
        console.log("aca llego")
        console.log(token, roomId, device)
        const response = await axios.post(`${API_URL}/${roomId}/devices`,
            {
                name: device.name,
                type: device.type
            },
            {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                }
            }
        );
        console.log("aca llego2")
        return response.data;
    } catch (error) {
        console.error('Error al agregar dispositivo:', error);
        throw error;
    }
};