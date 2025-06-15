import axios from 'axios';

const API_URL = "http://localhost:8080/user";

export const getUserRoleInCurrentHome = async (homeId, token) => {
    try {
        const response = await axios.get(`${API_URL}/current-role/${homeId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return  response.data;
    } catch (error) {
        console.error("Error al obtener el rol:", error);
        throw error;
    }
};
