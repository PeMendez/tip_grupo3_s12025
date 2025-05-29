import { X, ArrowLeft } from 'lucide-react';
import {FiEdit} from "react-icons/fi";
import styles from './styles/roundButton.module.css'

const RoundButton = ({ type = 'edit', onClick: handleClick }) => {

    return (
        <div className={styles.roundButton} onClick={handleClick}>
            {type === 'edit' ? (
                <FiEdit size={28}/>
            ) : type === 'arrow'? (
                <ArrowLeft size={28}  />
            ) : <X size={28}  /> }
        </div>
    );
};

export default RoundButton;
