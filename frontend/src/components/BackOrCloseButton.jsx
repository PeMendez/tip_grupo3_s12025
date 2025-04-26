import { useNavigate, useLocation } from 'react-router-dom';
import { X, ArrowLeft } from 'lucide-react';
import './BackOrCloseButton.css';

const BackOrCloseButton = ({ type = 'arrow' }) => {
    const navigate = useNavigate();
    const location = useLocation();
    const handleClick = () => {
        if (location.pathname !== '/home') {
            navigate(-1);
        } else {
            console.log("Si hiciera esto me desloguearía.");
        }
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
