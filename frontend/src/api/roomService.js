import axios from 'axios';

const API_URL = 'http://localhost:8080/room';

export const addDeviceToRoom = async (roomId, device, token) => {
    try {
        const response = await axios.post(
            `${API_URL}/${roomId}/devices`,
            {
                name: device.name,
                type: device.type,
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
