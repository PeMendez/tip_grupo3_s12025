import axios from 'axios';

const API_URL = "http://localhost:8080/user";

export const getRole = async (token) => {
   try {
       const response = await axios.get(`${API_URL}/role`, {
           headers: {
               'Authorization': `Bearer ${token}`
           }
       });
       return response.data;
   } catch (e) {
       console.log("Error al obtener el rol", e);
       throw e;
   }
};

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
