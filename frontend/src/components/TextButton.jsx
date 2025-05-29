import styles from './styles/textButton.module.css'
const textButton = ({text, handleClick}) => {


    return (
        <button className={styles.textButton} onClick={handleClick}>{text}</button>
    )
}
export default textButton