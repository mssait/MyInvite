import { createTheme } from "@mui/material"
import config from "../config"
const theme = createTheme({
    palette: {
        primary: {
            light: "#52B1FF20",
            main: "#52B1FF",
        },
        secondary: {
            light: "#FFFFFF",
            main: "#0C2950",
        },
        background: {
            default: "#FFFFFF",
            paper: "#FFFFFF",
        },
    },
    shape: {
        borderRadius: config.borderRadius
    },
    typography: {
        fontFamily: config.fontFamily,
        h6: {
            fontWeight: 500,
            fontSize: 12
        },
        h5: {
            fontSize: 14,
            fontWeight: 500
        },
        h4: {
            fontSize: 16,
            fontWeight: 600
        },
        h3: {
            fontSize: 18,
            fontWeight: 600
        },
        h2: {
            fontSize: 20,
            fontWeight: 700
        },
        h1: {
            fontSize: 22,
            fontWeight: 700
        },
        body1: {
            fontSize: 14,
            fontWeight: 400,
            lineHeight: '1.334em'
        },
    },
})
export default theme