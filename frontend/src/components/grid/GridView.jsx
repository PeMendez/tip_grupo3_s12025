import {useState} from 'react';
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
    const [nameItemToDelete, setNameToDelete] = useState(null);

    const handleItemClick = (item) => {
        if (editMode) {
            setNameToDelete(item.name);
            setItemToDelete(item);

        }
        onItemClick(item);
    };

    const handleConfirmDelete = () => {
        if (type === 'device') {
            onDelete(itemToDelete.id);
        } else if (type === 'room') {
            onDelete(itemToDelete);
        }
        setItemToDelete(null);
    };

    const getItemClass = (item) => {
        if (editMode) return 'room-button edit-mode';
        if (type === 'device') {
            if (item.ackStatus === true) return 'room-button ack';
            if (item.ackStatus === false) return 'room-button noAck';
        }
        return 'room-button';
    };

    return (
        <div className="main-container">
            <div className={'room-grid'}>
                {((type === 'room' && (editMode || addMode)) || type === 'device') && (
                    <BackOrCloseButton type="arrow" onClick={onClose} />
                )}
                {items.map((item, index) => (
                    <div
                        key={index}
                        className={getItemClass(item)}
                        onClick={() => handleItemClick(item)}
                    >
                        {type === 'device' ? (
                        <DeviceCard
                            key={index}
                            device={item}
                            toggleLight={item.ackStatus === true ? toggleLight : () => {}}
                            setBrightness={item.ackStatus === true ? setBrightness : () => {}}
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