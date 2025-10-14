import { createTheme } from "@mui/material"
import config from "../config"
const theme = createTheme({
    palette: {
        primary: {
            light: "#EBEBEF",
            main: "#373643",
        },
        secondary: {
            light: "#FFFFFF",
            main: "#CAEF35",
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
            fontSize: '0.75rem'
        },
        h5: {
            fontSize: '0.875rem',
            fontWeight: 500
        },
        h4: {
            fontSize: '1rem',
            fontWeight: 600
        },
        h3: {
            fontSize: '1.25rem',
            fontWeight: 600
        },
        h2: {
            fontSize: '1.5rem',
            fontWeight: 700
        },
        h1: {
            fontSize: '2.125rem',
            fontWeight: 700
        },
        subtitle1: {
            fontSize: '0.925rem',
            fontWeight: 500,
        },
        subtitle2: {
            fontSize: '0.75rem',
            fontWeight: 400,
        },
        caption: {
            fontSize: '0.75rem',
            fontWeight: 400
        },
        body1: {
            fontSize: '0.875rem',
            fontWeight: 400,
            lineHeight: '1.334em'
        },
        body2: {
            letterSpacing: '0em',
            fontWeight: 400,
            lineHeight: '1.5em',
        },
    },

    components: {
        MuiPaper: {
            defaultProps: {
                elevation: 0
            },
        },
        MuiCard: {
            defaultProps: {
                elevation: 0,
                boxShadow: 'none'
            },
        }
    }
})
export default theme