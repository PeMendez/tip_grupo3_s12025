import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL + "/user";

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

export const createUserHome = async (data, token) => {
    try {
        const response = await axios.post(`${API_URL}/create`, data,{
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return  response.data;
    } catch (e) {
        console.error("Error en createUserHome:", e.response?.data || e.message);
        throw e.response?.data || new Error('Error al procesar la solicitud');    }
};