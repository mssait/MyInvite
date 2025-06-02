import { Autocomplete, Box, CircularProgress, TextField } from "@mui/material";
import { useEffect, useState } from "react";

const ClientAutocomplete = (
    {
        name,
        required,
        id,
        label,
        onChange,
        value,
        inputValue,
        onInputChange,
        onBlur,
        multiple,
        filterSelectedOptions,
        options
    }) => {
    const [open, setOpen] = useState(false);
    const [selectValue, setSelectValue] = useState(multiple ? [] : null);
    const loading = open && options.length === 0;
    useEffect(() => {
        if (value) {
            if (multiple) {
                setSelectValue(options?.filter(o => (value.indexOf(o) > -1 || value.indexOf(o.id) > -1)))
            } else {
                setSelectValue(options?.filter(o => o === value || o.id === value)[0])
            }
        }

    }, [])

    return (
        <Autocomplete
            name={name}
            multiple={multiple}
            filterSelectedOptions={filterSelectedOptions}
            id={id}
            open={open}
            onOpen={() => setOpen(true)}
            onClose={() => setOpen(false)}
            getOptionLabel={option => option?.label || option}
            options={options}
            onBlur={() => {
                const value = multiple ? selectValue && selectValue.map(v => v?.id || v) : selectValue?.id
                onBlur({ target: { name, value } })
            }}
            value={selectValue}
            onChange={(e, o) => {
                setSelectValue(o)
                const value = multiple ? o?.map(v => v?.id || v) : o?.id || o
                onChange && onChange({ target: { name, value } }, value)
            }}
            inputValue={inputValue}
            onInputChange={onInputChange}
            renderInput={params => (
                <TextField
                    required={required}
                    id={`input-${id}`}
                    name={`input-${id}`}
                    {...params}
                    label={label}
                    InputLabelProps={{ style: { color: "#000", paddingLeft: '12px' } }}
                    InputProps={{
                        ...params.InputProps,
                        endAdornment:
                            <Box>
                                {loading && <CircularProgress color="inherit" size={20} />}
                                {params.InputProps.endAdornment}
                            </Box>
                    }}
                />
            )}
        />
    )
}
export default ClientAutocomplete
