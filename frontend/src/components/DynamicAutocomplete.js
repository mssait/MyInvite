import { Autocomplete, Box, CircularProgress, TextField } from "@mui/material";
import { useEffect, useState } from "react";
import fetcher from "../utils/fetcher";

const DynamicAutocomplete = (
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
        select
    }) => {
    const [open, setOpen] = useState(false);
    const [options, setOptions] = useState([]);
    const [selectValue, setSelectValue] = useState(multiple ? [] : null);
    const loading = open && options.length === 0;

    useEffect(() => {
        fetcher(`/api/dynamic-select/${select}`)
            .then(res => res.json())
            .then(data => {
                setOptions(data.options)
                if (value) {
                    if (multiple) {
                        setSelectValue(data.options?.filter(o => value.indexOf(o) > -1))
                    } else {
                        setSelectValue(data.options?.filter(o => o === value)[0])
                    }
                }
            })
            .catch(console.log)
    }, [])

    return (
        <Autocomplete
            name={name}
            multiple={multiple}
            freeSolo={true}
            filterSelectedOptions={filterSelectedOptions}
            id={id}
            open={open}
            onOpen={() => setOpen(true)}
            onClose={() => setOpen(false)}
            getOptionLabel={option => option?.label || option}
            options={options}
            onBlur={() => {
                const value = selectValue
                onBlur({ target: { name, value } })
            }}
            value={selectValue}
            onChange={(e, o) => {
                setSelectValue(o)
                onChange && onChange({ target: { name, value: o } }, o)
            }}
            inputValue={inputValue}
            onInputChange={(e, o) => {
                setSelectValue(o)
                onChange && onChange({ target: { name, value: o } }, o)
            }}
            renderInput={params => (
                <TextField
                    required={required}
                    id={`input-${id}`}
                    name={`input-${id}`}
                    {...params}
                    label={label}
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
export default DynamicAutocomplete