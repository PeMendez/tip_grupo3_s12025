import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BackOrCloseButton from "../components/BackOrCloseButton.jsx"
import { getHome, addRoom, deleteRoom } from '../api/homeService2.js'
import cocinaImg from '../assets/cocina.jpg'
import dormitorioImg from '../assets/dormitorio.jpg'
import livingImg from '../assets/living.jpg'
import garajeImg from '../assets/garaje.jpg'
import lavaderoImg from '../assets/lavadero.jpg'
import banoImg from '../assets/Baño.jpg'
import salaImg from '../assets/salaDeJuegos.jpg'
import { FiEdit, FiPlus } from 'react-icons/fi'
import './mainPage.css'

function MainPage() {
    const [rooms, setRooms] = useState([])
    const [editMode, setEditMode] = useState(false)
    const [editHome, setEditHome] = useState(false)
    const navigate = useNavigate()
    const [showDeletePopup, setShowDeletePopup] = useState(false)
    const [roomToDelete, setRoomToDelete] = useState(null)

    const roomTypes = [
        {name: 'Cocina', img: cocinaImg},
        {name: 'Dormitorio', img: dormitorioImg},
        {name: 'Living', img: livingImg},
        {name: 'Garaje', img: garajeImg},
        {name: 'Lavadero', img: lavaderoImg},
        {name: 'Baño', img: banoImg},
        {name: 'Sala de juegos', img: salaImg},
    ]

    const token = localStorage.getItem('token')

    const logout = () => {
        localStorage.removeItem('token');
        navigate("/");
    };

    useEffect(() => {
        const fetchRooms = async () => {
            try {
                const fetchedRooms = await getHome(token)
                setRooms(fetchedRooms.rooms || [])
            } catch (error) {
                console.error("Error al obtener habitaciones", error)
            }
        }
        fetchRooms()
    }, [token])

    const handleAddRoom = async (roomName) => {
        try {
            await addRoom(token, roomName)
            const updatedRooms = await getHome(token)
            setRooms(updatedRooms.rooms || [])
            setEditMode(false)
        } catch (error) {
            console.error("Error al agregar habitación", error)
        }
    }

    const handleConfirmDelete = async () => {
        if (!roomToDelete) return;
        try {
            await deleteRoom(token, roomToDelete.id)
            const updatedRooms = await getHome(token)
            setRooms(updatedRooms.rooms || [])
            setShowDeletePopup(false)
            setRoomToDelete(null)
        } catch (error) {
            console.error("Error al eliminar habitación", error)
        }
    }

    const handleCloseModal = (e) => {
        if (e.target === e.currentTarget) {
            setShowDeletePopup(false)
        }
    }

    const showRules = () => {
        navigate("/rules");
    }

    if (editMode) {
        return (
            <div className="main-container">
                <div className="header-wrapper">
                    <div className="header">
                        <BackOrCloseButton type="arrow" onClick={() => setEditMode(false)}/>
                        <h2>Agregar Habitaciones</h2>
                    </div>
                </div>
                <div className="room-grid">
                    {roomTypes.map((room, index) => (
                        <button key={index} onClick={() => handleAddRoom(room.name)} className="room-button">
                            <img src={room.img} alt={room.name}/>
                            <span>{room.name}</span>
                        </button>
                    ))}
                    <div className="add-room-icon">
                        <button onClick={() => {/* Falta agregar opcion hab personalizada */  }}>
                            <FiPlus size={24} className="icon"/>
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    if (editHome) {
        return (
            <div className="main-container">
                <div className="header-wrapper">
                    <div className="header">
                        <BackOrCloseButton type="arrow" onClick={() => setEditHome(false)}/>
                        <h2>Editar Mis Habitaciones</h2>
                    </div>
                </div>
                <div className="room-grid">
                    {rooms.map((room, index) => {
                        const type = roomTypes.find((r) => r.name === room.name);
                        return (
                            <div
                                key={index}
                                className="room-editable-container"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    setRoomToDelete(room);
                                    setShowDeletePopup(true);
                                }}
                            >
                                <div className="room-button edit-mode">
                                    <img src={type?.img || salaImg} alt={room.name} />
                                    <span>{room.name}</span>
                                    <div className="delete-icon-full">
                                        🗑️
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                    <div className="add-room-icon">
                        <button onClick={() => {
                            setEditMode(true);
                            setEditHome(false);
                        }}>
                            <FiPlus size={24} className="icon" />
                        </button>
                    </div>
                </div>

                {showDeletePopup && (
                    <div className="modal-backdrop" onClick={handleCloseModal}>
                        <div className="modal">
                            <p>¿Estás seguro que querés eliminar la habitación "{roomToDelete?.name}"?</p>
                            <div className="modal-actions">
                                <button onClick={handleConfirmDelete}>Confirmar</button>
                                <button onClick={() => setShowDeletePopup(false)}>Cancelar</button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        );
    }

    return (
        <div className="main-container">
            <div className="header-wrapper">
                <div className="header">
                    <BackOrCloseButton type="x" onClick={logout}/>
                    <h2>Mi Hogar</h2>
                </div>
            </div>
            <div className="">
                <div className="">
                    <button onClick={() => showRules()}>
                        Reglas
                    </button>
                </div>
            </div>

            {rooms.length > 0 ? (
                <>
                    <div className="edit-container">
                        <div className="edit-button">
                            <button onClick={() => setEditHome(true)}>
                                <FiEdit size={24}/>
                            </button>
                        </div>
                    </div>
                    <div className="room-grid">
                        {rooms.map((room, index) => {
                            const type = roomTypes.find((r) => r.name === room.name);
                            return (
                                <div
                                    key={index}
                                    className="room-button"
                                    onClick={() => navigate(`/room/${room.id}`)}
                                >
                                    <img src={type?.img || salaImg} alt={room.name}/>
                                    <span>{room.name}</span>
                                </div>
                            );
                        })}
                    </div>
                </>
            ) : (
                <div className="no-rooms">
                    <p>Aún no tenés habitaciones...</p>
                    <button onClick={() => setEditMode(true)}>
                        <FiEdit/> Editar hogar
                    </button>
                </div>
            )}
        </div>
    )
}

export default MainPage;