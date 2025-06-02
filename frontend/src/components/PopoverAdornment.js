import { IconButton, Popover, Typography } from "@mui/material";
import { IconHelp } from "@tabler/icons-react";
import { useState } from "react";
import { v4 as uuidv4 } from 'uuid';

const PopoverAdornment = ({
    content,
    Icon = IconHelp
}) => {
    const [anchorEl, setAnchorEl] = useState(null)
    const id = uuidv4()
    return (
        <>
            <IconButton aria-describedby={id} onClick={(event) => {
                setAnchorEl(event.currentTarget)
            }}>
                <Icon />
            </IconButton>
            <Popover
                id={id}
                open={Boolean(anchorEl)}
                anchorEl={anchorEl}
                onClose={() => {
                    setAnchorEl(null)
                }}
                anchorOrigin={{
                    vertical: 'bottom',
                    horizontal: 'right',
                }}
            >
                <Typography p={2}>{content}</Typography>
            </Popover>
        </>
    )
}
export default PopoverAdornment