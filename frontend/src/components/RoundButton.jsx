import { X, ArrowLeft } from 'lucide-react';
import {FiEdit, FiPlus} from "react-icons/fi";
import {FaDoorOpen} from 'react-icons/fa';
import styles from './styles/roundButton.module.css'

const RoundButton = ({ type = 'edit', onClick: handleClick }) => {

    return (
        <div className={styles.roundButton} onClick={handleClick}>
            {type === 'edit' ? (
                <FiEdit size={28}/>
            ) : type === 'arrow'? (
                <ArrowLeft size={28}  />
            ) : type === 'plus'? (
                <FiPlus size={28}  />
            ) : type === 'exit'? (
                <FaDoorOpen size={28} />
            ) : <X size={28}  /> }
        </div>
    );
};

export default RoundButton;
