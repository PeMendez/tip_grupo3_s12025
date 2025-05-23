import axios from 'axios';

const API_URL = "http://localhost:8080/auth";

export const login = async (username, password) => {
    const response = await axios.post(`${API_URL}/login`, { username, password });
    const token = response.data.token;
    localStorage.setItem('token', token);
    return response.data;
};

export const register = async (username, password) => {
    const response = await axios.post(`${API_URL}/register`, { username, password });
    const token = response.data.token;
    localStorage.setItem('token', token);
    return response.data;
};

export const logout = () => {
    localStorage.removeItem('token');
};
