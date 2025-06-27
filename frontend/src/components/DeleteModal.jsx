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
                        <label className="styled-checkbox tooltip-container">
                            <input
                                type="checkbox"
                                checked={factoryReset}
                                onChange={(e) => setFactoryReset(e.target.checked)}
                            />
                            <span className="checkmark"></span>
                            <span className="checkbox-label">Eliminar el dispositivo</span>
                            <div className="tooltip-text">
                                Si marcás esta opción, el dispositivo será eliminado completamente del hogar.
                                Solo se podrá reconfigurar haciendo un factory reset desde el dispositivo físico.
                            </div>
                        </label>
                    </div>
                )}
                <div className="modal-actions">
                    <TextButton text={"Confirmar"} handleClick={() => handleConfirm(factoryReset)}/>
                    <TextButton text={"Cancelar"} handleClick={onCancel}/>
                </div>
            </div>
        </div>
    );
};

export default DeleteModal;