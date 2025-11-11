import config from "../config"
import logo from "../images/logo.png"

const Logo = () => (
    <img
        style={{
            maxWidth: "100%",
            height: "auto",
            maxHeight: 150,
            display: "block",
            borderRadius: config.borderRadius
        }}
        src={logo} />
)

export default Logo
    