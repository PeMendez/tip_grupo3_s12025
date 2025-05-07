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