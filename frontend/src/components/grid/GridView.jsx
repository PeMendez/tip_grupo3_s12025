import { useState } from 'react';
import BackOrCloseButton from "../BackOrCloseButton";
import TextButton from "../TextButton";
import DeleteModal from "../DeleteModal";
import { FiPlus } from 'react-icons/fi';
import '../../pages/styles/mainPage.css';

const GridView = ({
                      type,
                      items,
                      onItemClick,
                      onAdd,
                      onClose,
                      editMode,
                      onDelete,
                      getImage,
                      getIcon
                  }) => {
    const [itemToDelete, setItemToDelete] = useState(null);

    const handleItemClick = (item) => {
        if (editMode) {
            setItemToDelete(item);
        } else {
            onItemClick(item);
        }
    };

    const handleConfirmDelete = () => {
        onDelete(itemToDelete.id);
        setItemToDelete(null);
    };

    return (
        <div className="main-container">
            <BackOrCloseButton type="arrow" onClick={onClose} />

            <div className={type === 'room' ? 'room-grid' : 'room2-grid'}>
                {items.map((item, index) => (
                    <div
                        key={index}
                        className={`${type === 'room' ? 'room' : 'device'}-button ${editMode ? 'edit-mode' : ''}`}
                        onClick={() => handleItemClick(item)}
                    >
                        {type === 'room' ? (
                            <>
                                <img src={getImage(item)} alt={item.name} />
                                <span>{item.name}</span>
                            </>
                        ) : (
                            <>
                                <div className="device-icon">{getIcon(item.type)}</div>
                                <span>{item.name}</span>
                            </>
                        )}
                        {editMode && (
                            <div className="delete-icon-full">🗑️</div>
                        )}
                    </div>
                ))}

                {onAdd && (
                    <div className={`add-${type}-icon`}>
                        {type === 'room' ? (
                            <TextButton handleClick={onAdd} text="Nueva" />
                        ) : (
                            <button onClick={onAdd}>
                                <FiPlus size={24} className="icon" />
                            </button>
                        )}
                    </div>
                )}
            </div>

            {itemToDelete && (
                <DeleteModal
                    device={itemToDelete}
                    onConfirm={handleConfirmDelete}
                    onCancel={() => setItemToDelete(null)}
                    message={`¿Estás seguro que querés eliminar la ${type === 'room' ? 'habitación' : 'dispositivo'} "${itemToDelete.name}"?`}
                />
            )}
        </div>
    );
};

export default GridView;