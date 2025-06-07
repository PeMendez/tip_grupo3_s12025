import './styles/deleteModal.css'
import TextButton from "./TextButton.jsx";

const DeleteModal = ({ onConfirm, onCancel, message }) => {
    return (
        <div className="modal-backdrop" onClick={onCancel}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <p>{message}</p>
                <div className="modal-actions">
                    <TextButton text={"Confirmar"} handleClick={onConfirm}/>
                    <TextButton text={"Cancelar"} handleClick={onCancel}/>
                </div>
            </div>
        </div>
    );
};

export default DeleteModal;