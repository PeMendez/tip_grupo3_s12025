import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getHome, addRoom, deleteRoom } from '../api/homeService.js';
import GridView from '../components/grid/GridView';
import RoundButton from '../components/RoundButton';
import TextButton from '../components/TextButton';
import cocinaImg from '../assets/cocina.jpg'
import dormitorioImg from '../assets/dormitorio.jpg'
import livingImg from '../assets/living.jpg'
import garajeImg from '../assets/garaje.jpg'
import lavaderoImg from '../assets/lavadero.jpg'
import banoImg from '../assets/Baño.jpg'
import salaImg from '../assets/salaDeJuegos.jpg'
import './styles/mainPage.css';
import usePushNotifications from "../hooks/usePushNotifications.js";
import {useTitle} from "../contexts/TitleContext.jsx";
import { useAuth } from "../contexts/AuthContext";
import {getUserRoleInCurrentHome} from "../api/userHomeService.js";

const roomImages = {
    'Cocina': cocinaImg,
    'Dormitorio': dormitorioImg,
    'Living': livingImg,
    'Garaje': garajeImg,
    'Lavadero': lavaderoImg,
    'Baño': banoImg,
    'Sala de juegos': salaImg
};

const MainPage = () => {
    const [rooms, setRooms] = useState([]);
    const [mode, setMode] = useState('view'); // 'view' | 'add' | 'edit' | 'noHome'
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const {isSubscribed,error} = usePushNotifications(); //Sin esto no andan las push
    //usar el flag y el msg error para mostrar un toast?
    const {setHeaderTitle} = useTitle();
    const { setRole } = useAuth();

    useEffect(() => {
        const titles = {
            'view': "Mi Hogar",
            'add': "Agregar Habitaciones",
            'edit': "Editar Mis Habitaciones"
        };
        setHeaderTitle(titles[mode]);
    }, [mode]);

    useEffect(() => {
        const fetchRooms = async () => {
            try {
                const fetchedRooms = await getHome(token);

                if (!fetchedRooms) {
                    setMode('noHome');
                    return;
                }
                setRooms(fetchedRooms.rooms || []);
                const response = await getUserRoleInCurrentHome(fetchedRooms.id, token)
                setRole(response);
                console.log(home)
            } catch (error) {
                console.error("Error al obtener habitaciones", error);
            }
        };
        fetchRooms();
    }, [token]);

    const handleAddRoom = async (roomName) => {
        try {
            await addRoom(token, roomName);
            const updatedRooms = await getHome(token);
            setRooms(updatedRooms.rooms || []);
            setMode('view');
        } catch (error) {
            console.error("Error al agregar habitación", error);
        }
    };

    const handleDeleteRoom = async (room) => {
        try {
            await deleteRoom(token, room.id);
            const updatedRooms = await getHome(token);
            setRooms(updatedRooms.rooms || []);
        } catch (error) {
            console.error("Error al eliminar habitación", error);
        }
    };

    if (mode === 'noHome') {
        navigate('/noHome');
    }

    if (mode === 'add') {
        return (
            <GridView
                type="room"
                items={Object.entries(roomImages).map(([name, img]) => ({ name, img }))}
                onItemClick={(room) => handleAddRoom(room.name)}
                onClose={() => setMode('view')}
                getImage={(room) => room.img}
                addMode={true}
            />
        );
    }

    if (mode === 'edit') {
        return (
            <GridView
                type="room"
                items={rooms}
                editMode={true}
                onDelete={handleDeleteRoom}
                onItemClick={(room) => console.log("solo para que no rompa")}
                onAdd={() => setMode('add')}
                onClose={() => setMode('view')}
                getImage={(room) => roomImages[room.name] || salaImg}
            />
        );
    }

    return (
        <div className="main-container">
            <div className="rulesButton">
                <TextButton handleClick={() => navigate("/rules")} text="Reglas"/>
            </div>

            {rooms.length > 0 ? (
                <>
                    <div className="edit-container">
                        <RoundButton type="edit" onClick={() => setMode('edit')}/>
                    </div>
                    <GridView
                        type="room"
                        items={rooms}
                        onItemClick={(room) => navigate(`/room/${room.id}`)}
                        getImage={(room) => roomImages[room.name] || salaImg}
                    />
                </>
            ) : (
                <div className="no-rooms">
                    <p>Aún no tenés habitaciones...</p>
                    <RoundButton type="edit" onClick={() => setMode('add')}/>
                </div>
            )}
        </div>
    );
};

export default MainPage;