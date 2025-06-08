import './styles/deleteModal.css'
import TextButton from "./TextButton.jsx";
import {useState} from "react";

const DeleteModal = ({ onConfirm, onCancel, message, type }) => {
    const [factoryReset, setFactoryReset] = useState(false);

    const handleConfirm = (factoryReset) => {
       onConfirm(factoryReset);
    }

    return (
        <div className="modal-backdrop" onClick={onCancel}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <p>{message}</p>
                {type === 'device' && (
                    <div className="styled-checkbox-container">
                        <label className="styled-checkbox">
                            <input
                                type="checkbox"
                                checked={factoryReset}
                                onChange={(e) => setFactoryReset(e.target.checked)}
                            />
                            <span className="checkmark"></span>
                            <span className="checkbox-label">Eliminar el dispositivo</span>
                        </label>
                    </div>
                )}
                <div className="modal-actions">
                    <TextButton text={"Confirmar"} handleClick={handleConfirm}/>
                    <TextButton text={"Cancelar"} handleClick={onCancel}/>
                </div>
            </div>
        </div>
    );
};

export default DeleteModal;