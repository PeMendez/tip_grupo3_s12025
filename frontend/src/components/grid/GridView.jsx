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
                      editMode,
                      onDelete,
                      onResetFactory,
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

    const handleConfirmDelete = (factoryReset) => {
        if (factoryReset && type === 'device') {
            onResetFactory(itemToDelete.id);
        } else {
            onDelete(type === 'device' ? itemToDelete.id : itemToDelete);
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
                                toggleLight={item.ackStatus && item.type === 'smart_outlet' ? toggleLight : () => {
                                    console.log("toggle light nt");
                                }}
                                setBrightness={item.ackStatus && item.type === 'dimmer' ? setBrightness : () => {
                                    console.log("set brillo nt");
                                }}
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
                        type={type}
                        message={`¿Estás seguro que querés  ${type === 'room' ? 'eliminar la habitación' : 'desconfigurar el dispositivo'} "${nameItemToDelete}"?`}
                    />
                )}
            </div>
        </div>
    );
};

export default GridView;