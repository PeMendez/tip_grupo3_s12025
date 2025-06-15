import {useState} from 'react'
import './styles/noHome.css';
import TextButton from "../components/TextButton.jsx";
import { useTitle } from "../contexts/TitleContext.jsx";
import { useEffect } from "react";
import HomeActionPopup from "../components/HomeActionPopup.jsx";
import {useNavigate} from "react-router-dom";

const NoHomePage = () => {
    const { setHeaderTitle } = useTitle();
    const [showPopup, setShowPopup] = useState(false);
    const [action, setAction] = useState('')
    const navigate = useNavigate()

    useEffect(() => {
        setHeaderTitle("NeoHub");
    }, []);

    const handleSuccess = (response) => {
        console.log('Hogar creado:', response);
        setShowPopup(false);
        navigate('/home')
    };


    return (
        <div className="no-home-container">
            <h2>No estás asociado a ningún hogar</h2>
            <p>Podés crear uno nuevo o unirte a uno existente.</p>
            <div className="no-home-buttons">
                <TextButton
                    text="Crear nuevo hogar"
                    handleClick={() => {
                        setShowPopup(true)
                        setAction('CREATE')
                    }}
                />
                <TextButton
                    text="Unirse a un hogar"
                    handleClick={() => {
                        setShowPopup(true)
                        setAction('JOIN')
                    }}
                />
            </div>

            {showPopup && (
                <HomeActionPopup
                    action={action}
                    onClose={() => setShowPopup(false)}
                    onSuccess={handleSuccess}
                />
            )}
        </div>
    );
};

export default NoHomePage;