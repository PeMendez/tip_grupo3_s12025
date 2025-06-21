import axios from 'axios';

const API_URL = 'http://localhost:8080/rule';

export const getRuleForDevice = async (deviceId, token) => {
    try {
        const response = await axios.get(`${API_URL}/${deviceId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error al obtener las reglas del dispositivo:', error);
        throw error;
    }
};

export const createRule = async (newRule, token) => {
    try {
        const response = await axios.post(API_URL, newRule, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error al crear la regla:', error);
        throw error;
    }
};

export const deleteRule = async (ruleId, token) => {
    try {
        const response = await axios.delete(`${API_URL}/${ruleId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error al eliminar la regla:', error);
        throw error;
    }
};

export const getAttributes = async (deviceType, token) =>{
    try {
        const response = await axios.get(`${API_URL}/${deviceType}/attributes`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (e) {
        console.log('Error al obtener los atributos:', e);
        throw e;
    }
};

export const getOperators = async (attributeType, token) => {
    try {
        const response = await axios.get(`${API_URL}/${attributeType}/operators`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (e) {
        console.log('Error al obtener los operadores', e)
        throw e;
    }
};

export const getActions = async (deviceType, token) => {
    try {
        const response = await axios.get(`${API_URL}/${deviceType}/actions`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (e) {
        console.log('Error al obtener las acciones disponibles', e)
        throw e;
    }
};

export const getAllRules = async (token) => {
    try {
        const response = await axios.get(`${API_URL}/allRules`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            }
        });
        return response.data;
    } catch (e) {
        console.log('Error al obtener todas las reglas', e);
        throw e;

    }
};