import {useEffect, useState} from 'react';
import BackOrCloseButton from "../BackOrCloseButton";
import TextButton from "../TextButton";
import DeleteModal from "../DeleteModal";
import { FiPlus } from 'react-icons/fi';
import DeviceCard from '../../components/grid/DeviceCard.jsx';
import './styles/gridView.css'

const GridView = ({
                      type,
                      items,
                      onItemClick,
                      onAdd,
                      onClose,
                      editMode,
                      onDelete,
                      getImage,
                      toggleLight,
                      setBrightness,
                      addMode
                  }) => {
    const [itemToDelete, setItemToDelete] = useState(null);
    const [nameItemToDelete, setNameToDelete] = useState(null)

    const handleItemClick = (item) => {
        if (editMode) {
            setNameToDelete(item.name);

            if (type === 'device') {
                setItemToDelete(item);
            } else if (type === 'room') {
                setItemToDelete(item);
            }
        }
        onItemClick(item);
    };

    const handleConfirmDelete = () => {
        onDelete(itemToDelete.id);
        setItemToDelete(null);
    };

    useEffect(() => {
        console.log(items)
    }, []);

    return (
        <div className="main-container">
            <BackOrCloseButton type="arrow" onClick={onClose} />

            <div className={'room-grid'}>
                {items.map((item, index) => (
                    <div
                        key={index}
                        className={`room-button${editMode ? ' edit-mode' : ''}${!editMode && type === 'device' && item.ackStatus ? ' ack' : ''}${!editMode && type === 'device' && !item.ackStatus ? ' noAck' : ''}`}
                        onClick={() => handleItemClick(item)}
                    >
                        {type === 'device' ? (
                        <DeviceCard
                            key={index}
                            device={item}
                            toggleLight={toggleLight}
                            setBrightness={setBrightness}
                            onClick={() => handleItemClick(item)}
                            editMode={editMode}
                            addMode={addMode}
                        />
                        ) : (
                            <>
                                <img src={getImage(item)} alt={item.name}/>
                                <span>{item.name}</span>
                                </>
                        )}

                            {editMode && (
                                <div className="delete-icon-full">🗑️</div>
                            )}

                            {!editMode && !item.ackStatus && (
                            <div className="delete-icon-full">🔌</div>
                            )}

                        </div>
                     ))}

                        {onAdd && (
                            <div className={`add-room-icon`}>
                                {type === 'room' ? (
                                    <TextButton handleClick={onAdd} text="Nueva"/>
                                ) : (
                                    <TextButton handleClick={onAdd} text={<FiPlus size={24} className="icon"/>}/>
                                )}
                            </div>
                        )}


                {itemToDelete && (
                    <DeleteModal
                    device={itemToDelete}
                onConfirm={handleConfirmDelete}
                onCancel={() => setItemToDelete(null)}
                    message={`¿Estás seguro que querés eliminar ${type === 'room' ? 'la habitación' : 'el dispositivo'} "${nameItemToDelete}"?`}
                />
            )}
            </div>
        </div>
    );
};

export default GridView;