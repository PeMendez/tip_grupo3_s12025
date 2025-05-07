let socket = null;
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;

const connectWebSocket = (onMessageReceived) => {
    if (socket) {
        disconnectWebSocket();
    }

    if (socket && socket.readyState === WebSocket.OPEN) {
        return;
    }

    const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws/mqtt';
    console.log(`Conectando a WebSocket en: ${wsUrl}`);

    socket = new WebSocket(wsUrl);

    const connectionTimeout = setTimeout(() => {
        if (socket.readyState !== WebSocket.OPEN) {
            console.error('Timeout de conexión WebSocket');
            socket.close();
        }
    }, 5000);

    socket.onopen = () => {
        clearTimeout(connectionTimeout);
        console.log('Conexión WebSocket establecida');
        reconnectAttempts = 0;
    };

    socket.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            onMessageReceived(data);
        } catch (error) {
            console.error('Error procesando mensaje:', error);
        }
    };

    socket.onerror = (error) => {
        console.error('Error en WebSocket:', error);
    };

    socket.onclose = () => {
        if (reconnectAttempts < maxReconnectAttempts) {
            const delay = Math.min(1000 * reconnectAttempts, 5000);
            console.log(`Intentando reconectar en ${delay}ms...`);
            reconnectAttempts++;
            setTimeout(() => connectWebSocket(onMessageReceived), delay);
        } else {
            console.error('Máximo de intentos de reconexión alcanzado');
        }
    };
};

const disconnectWebSocket = () => {
    if (socket) {
        if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
            socket.onclose = () => {
                console.log('WebSocket cerrado limpiamente');
                socket = null;
            };
            socket.close();
        } else {
            socket = null;
        }
    }
};

export { connectWebSocket, disconnectWebSocket };