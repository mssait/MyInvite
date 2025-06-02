import { IconButton, TextField, Tooltip } from "@mui/material";
import { IconEye, IconEyeOff } from '@tabler/icons-react';
import { useState } from "react";

const PasswordField = props => {
    const [show, setShow] = useState(false)

    return (
        <TextField
            {...props}
            type={show ? 'text' : 'password'}
            slotProps={{
                input: {
                    endAdornment: (
                        <Tooltip
                            title={show ? "Hide Password" : "Show Password"}>
                            <IconButton
                                edge="end"
                                tabIndex={-1}
                                onClick={() => {
                                    setShow(show => !show)
                                }}>
                                {show ? <IconEyeOff /> : <IconEye />}
                            </IconButton>
                        </Tooltip>
                    )
                }
            }}
        />
    )
}
export default PasswordField;