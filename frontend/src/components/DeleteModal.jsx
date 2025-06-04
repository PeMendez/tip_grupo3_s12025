const DeleteModal = ({ device, onConfirm, onCancel }) => {
    return (
        <div className="modal-backdrop" onClick={onCancel}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <p>¿Eliminar "{device?.name}"?</p>
                <div className="modal-actions">
                    <button onClick={onConfirm}>Confirmar</button>
                    <button onClick={onCancel}>Cancelar</button>
                </div>
            </div>
        </div>
    );
};

export default DeleteModal;