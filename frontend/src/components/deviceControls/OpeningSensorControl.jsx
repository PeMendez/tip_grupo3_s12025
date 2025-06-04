const OpeningSensorControl = ({ device }) => {
    return (
        <div className="device-info">
            <small>{device.status ? "Abierto" : "Cerrado"}</small>
        </div>
    );
};

export default OpeningSensorControl;