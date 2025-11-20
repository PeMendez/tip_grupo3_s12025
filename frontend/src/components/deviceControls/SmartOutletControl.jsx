
const SmartOutletControl = ({ device, toggleLight }) => {
    return (
        <div className="switch-container" onClick={(e) => e.stopPropagation()}>
            <label className="switch">
                <input
                    type="checkbox"
                    checked={!!device.status}
                    onChange={() =>{
                        toggleLight(device)
                    }}
                />
                <span className={`slider round ${device.status ? 'on' : 'off'}`}></span>
            </label>
        </div>
    );
};

export default SmartOutletControl;