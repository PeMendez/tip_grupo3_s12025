import './styles/noHome.css';
import TextButton from "../components/TextButton.jsx";
import { useNavigate } from 'react-router-dom';
import {useTitle} from "../contexts/TitleContext.jsx";
import {useEffect} from "react";

const NoHomePage = () => {
    const navigate = useNavigate();
    const {setHeaderTitle} = useTitle();

    useEffect(() => {
        setHeaderTitle("NeoHub");
    }, []);

    return (
        <div className="no-home-container">
            <h2>No estás asociado a ningún hogar</h2>
            <p>Podés crear un nuevo hogar o unirte a uno existente.</p>
            <div className="no-home-buttons">
                <TextButton text="Crear nuevo hogar" handleClick={() => navigate("/create-home")} />
                <TextButton text="Unirse a un hogar" handleClick={() => navigate("/join-home")} />
            </div>
        </div>
    );
};

export default NoHomePage;
