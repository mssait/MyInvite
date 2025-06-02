import { InputLabel, OutlinedInput } from "@mui/material";
import { IMaskInput } from "react-imask";
import { forwardRef } from "react";

const TextMaskCustom = forwardRef(function TextMaskCustom(props, ref) {
    const { onChange, name, ...other } = props;
    return (
        <IMaskInput
            {...other}
            inputRef={ref}
            onAccept={value => onChange({ target: { name, value } })}
            overwrite
        />
    );
});

const MaskedField = props => {
    return (
        <>
            <InputLabel>{props.label}</InputLabel>
            <OutlinedInput {...props} inputComponent={TextMaskCustom} inputProps={props} />
        </>
    )
}
export default MaskedField