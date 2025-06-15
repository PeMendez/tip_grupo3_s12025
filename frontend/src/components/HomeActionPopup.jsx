import {useState} from "react";
import TextButton from "./TextButton.jsx";
import {createUserHome} from "../api/userHomeService.js";

const HomeActionPopup = ({ action, onClose, onSuccess }) => {
    const [homeName, setHomeName] = useState('');
    const [accessKey, setAccessKey] = useState('');
    const [error, setError] = useState('');
    const token = localStorage.getItem("token")

    const handleSubmit = async () => {
        if (!homeName) {
            setError('El nombre del hogar es obligatorio');
            return;
        }

        if (action === 'JOIN' && !accessKey) {
            setError('La clave de acceso es obligatoria');
            return;
        }

        try {
            const response = await createUserHome( {
                homeName: homeName,
                accessKey: action === 'CREATE' ? generateRandomKey() : accessKey,
                action: action
            }, token)
            onSuccess(response);
        } catch (err) {
            setError(err.response?.data?.message);
            console.log(err);
        }
    };

    const generateRandomKey = () => {
        return Math.random().toString(36).substring(2, 10).toUpperCase();
    }

    return (
        <div className="modal-backdrop">
            <div className="modal">
                <h3>{action === 'CREATE' ? 'Crear nuevo hogar' : 'Unirse a un hogar'}</h3>

                <input
                    type="text"
                    placeholder="Nombre del hogar"
                    value={homeName}
                    onChange={(e) => setHomeName(e.target.value)}
                />

                {action === 'JOIN' && (
                    <input
                        type="text"
                        placeholder="Clave de acceso"
                        value={accessKey}
                        onChange={(e) => setAccessKey(e.target.value)}
                    />
                )}

                {error && <div className="error-message">{error}</div>}

                <div className="modal-actions">
                    <TextButton
                        text={action === 'CREATE' ? 'Crear' : 'Unirse'}
                        handleClick={handleSubmit}
                    />
                    <TextButton text="Cancelar" handleClick={onClose}/>
                </div>
            </div>
        </div>
    );
};

export default HomeActionPopup;