import axios from 'axios';

export const controlLight = async (device, forceState) => {
    const apiUrl = "http://localhost:8080";

    try {
        const response = await axios.post(`${apiUrl}/api/mqtt/send`,
            new URLSearchParams({
                topic: device.topic,
                message: forceState
            }),
            {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            }
        );
        return response.data;
    } catch (error) {
        console.error('Error al controlar luces:', error);
        throw error;
    }
};
