import styles from './styles/textButton.module.css'
const TextButton = ({text, handleClick, ...rest }) => {


    return (
        <button className={styles.textButton} onClick={handleClick} {...rest} >{text}</button>
    )
}
export default TextButton