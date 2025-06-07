import './styles/deleteModal.css'
import TextButton from "./TextButton.jsx";

const DeleteModal = ({ device, onConfirm, onCancel }) => {
    return (
        <div className="modal-backdrop" onClick={onCancel}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <p>¿Eliminar "{device?.name}"?</p>
                <div className="modal-actions">
                    <TextButton text={"Confirmar"} handleClick={onConfirm}/>
                    <TextButton text={"Cancelar"} handleClick={onCancel}/>
                </div>
            </div>
        </div>
    );
};

export default DeleteModal;