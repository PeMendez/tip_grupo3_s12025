import { useNavigate } from 'react-router-dom';
import { X, ArrowLeft } from 'lucide-react';
import './BackOrCloseButton.css';

const BackOrCloseButton = ({ type = 'arrow' }) => {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(-1);
    };

    return (
        <div className={`back-or-close ${type}`}>
            {type === 'arrow' ? (
                <ArrowLeft size={28} onClick={handleClick} />
            ) : (
                <X size={28} onClick={handleClick} />
            )}
        </div>
    );
};

export default BackOrCloseButton;
