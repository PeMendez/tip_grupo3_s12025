import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BackOrCloseButton from "../components/BackOrCloseButton.jsx"
import { getHome, addRoom } from '../api/homeService2.js'
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
    const navigate = useNavigate()

    const roomTypes = [
        { name: 'Cocina', img: cocinaImg },
        { name: 'Dormitorio', img: dormitorioImg },
        { name: 'Living', img: livingImg },
        { name: 'Garaje', img: garajeImg },
        { name: 'Lavadero', img: lavaderoImg },
        { name: 'Baño', img: banoImg },
        { name: 'Sala de juegos', img: salaImg },
    ]

    const token = localStorage.getItem('token')

    useEffect(() => {
        const fetchRooms = async () => {
            try {
                const fetchedRooms = await getHome(token)
                console.log("Respuesta de getHome:", fetchedRooms)
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

    return (
        <div className="main-container">
            <div className="header-wrapper" >
                <div className="header">
                    <BackOrCloseButton
                        onClick={() => {
                            if (editMode) {
                                setEditMode(false);
                            } else {
                                navigate(-1);
                            }
                        }}
                    />
                    <h2>Mi Hogar</h2>
                </div>
            </div>

                {!editMode && (
                    <>
                        {rooms.length > 0 ? (
                            <>
                                <div className="room-grid">
                                    {rooms.map((room, index) => {
                                        const type = roomTypes.find(r => r.name === room.name)
                                        return (
                                            <div key={index}
                                                 className="room-button"
                                                 onClick={() => navigate(`/room/${room.name}`)}>
                                                <img src={type?.img || salaImg} alt={room.name}/>
                                                <span>{room.name}</span>
                                            </div>
                                        )
                                    })}
                                </div>
                                <div className="edit-button">
                                    <button onClick={() => setEditMode(true)}>
                                        <FiEdit size={24}/>
                                    </button>
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
                    </>
                )}

                {editMode && (
                    <>
                        <h3>Agregar habitaciones</h3>
                        <div className="room-grid">
                            {roomTypes.map((room, index) => (
                                <button key={index} onClick={() => handleAddRoom(room.name)} className="room-button">
                                    <img src={room.img} alt={room.name}/>
                                    <span>{room.name}</span>
                                </button>
                            ))}
                            {/* Podrías agregar lógica para habitaciones personalizadas luego */}
                            <div className="add-room-icon">
                                <button onClick={() => alert("Agregar habitación personalizada")}>
                                    <FiPlus size={24} className="icon"/>
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>
            )
            }

            export default MainPage
