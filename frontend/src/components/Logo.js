import config from "../config"

const Logo = () => (
    <img
        style={{
            maxWidth: "100%",
            height: "auto",
            maxHeight: 150,
            display: "block",
            borderRadius: config.borderRadius
        }}
        src="/logo.png" />
)

export default Logo
