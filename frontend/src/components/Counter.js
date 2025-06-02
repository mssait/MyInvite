import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import { Button, ButtonGroup } from "@mui/material";
import { useState } from 'react';

const Counter = ({
    value,
    min = 1,
    max = 10,
    onChange,
    name,
    size = 'small',
    disabled = false,
}) => {
    const [textValue, setTextValue] = useState(value)

    return (
        <ButtonGroup color="inherit" size={size}>
            <Button
                onClick={() => {
                    if (!disabled && value > min) {
                        setTextValue(value => value - 1)
                        onChange && onChange({ target: { name, value: value - 1 } }, value - 1)
                    }
                }}>
                <RemoveIcon />
            </Button>
            <Button disableRipple>{textValue}</Button>
            <Button
                onClick={() => {
                    if (!disabled && value < max) {
                        setTextValue(value => value + 1)
                        onChange && onChange({ target: { name, value: value + 1 } }, value + 1)
                    }
                }}>
                <AddIcon />
            </Button>
        </ButtonGroup >
    )
}
export default Counter