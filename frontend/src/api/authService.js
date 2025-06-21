import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL + "/auth";

export const login = async (username, password) => {
    const response = await axios.post(`${API_URL}/login`, { username, password });
    const token = response.data.token;
    localStorage.setItem('token', token);
    console.log("Log in exitoso.");
    return response.data;
};

export const register = async (data) => {
    const response = await axios.post(`${API_URL}/register`, data);
    const token = response.data.token;
    localStorage.setItem('token', token);
    console.log("Registro exitoso.");
    return response.data;
};

export const logout = () => {
    localStorage.removeItem('token');
    console.log("Log out exitoso.");
};
