import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL + "/home";

export const getHome = async (token) => {
    try {
        const response = await axios.get(API_URL, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.log(error)
        throw error;
    }
};

export const addRoom = async (token, roomName) => {
    const response = await axios.post(`${API_URL}/rooms`, { name: roomName }, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    return response.data;
}

export const deleteRoom = async (token, roomId) => {
    const response = await axios.post(`${API_URL}/rooms/${roomId}`, {
        roomId: roomId
    },{
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    return response.data;
};

export const getAllMembers = async (token, homeId) => {
    try {
        const response = await axios.get(`${API_URL}/${homeId}/members`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (e) {
        console.log(e)
        throw e;
    }
}

export const deleteMember = async  (homeId, userId, token) => {
    try {
        const response = await axios.put(`${API_URL}/${homeId}/members/${userId}`, {},{
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.data

    } catch (e) {
        console.log(e);
        throw e;
    }
}