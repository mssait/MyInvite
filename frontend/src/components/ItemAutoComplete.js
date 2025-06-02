import { useEffect, useState } from "react";
import { Autocomplete, Box, CircularProgress, TextField } from "@mui/material";
import fetcher from "../utils/fetcher";

const ItemAutocomplete = (
    {
        name,
        required,
        select,
        id,
        label,
        onChange,
        onBlur,
        value,
        multiple,
        filterSelectedOptions
    }) => {
    const [open, setOpen] = useState(false);
    const [options, setOptions] = useState([])
    const loading = open && options.length === 0;
    const [selectValue, setSelectValue] = useState(multiple ? [] : null);
    useEffect(() => {
        if (options.length === 0) {
            fetcher(`/api/select/${select}`)
                .then(res => res.json())
                .then(data => {
                    setOptions(data.options)
                    if (value) {
                        if (multiple) {
                            setSelectValue(data.options?.filter(o => value.indexOf(o.id) > -1))
                        } else {
                            setSelectValue(data.options?.filter(o => o.id === value)[0])
                        }
                    }
                })
                .catch(console.log)
        }
    }, [options.length, select, value, multiple])
    return (
        <Autocomplete
            name={name}
            multiple={multiple}
            filterSelectedOptions={filterSelectedOptions}
            id={id}
            open={open}
            onOpen={() => setOpen(true)}
            onClose={() => setOpen(false)}
            getOptionLabel={(option) => option?.label}
            options={options}
            loading={loading}
            onBlur={() => {
                const value = multiple ? selectValue && selectValue.map(v => v.id) : selectValue?.id
                onBlur({ target: { name: name, value: value } })
            }}
            value={selectValue}
            onChange={(e, o) => {
                setSelectValue(o)
                const value = multiple ? o?.map(v => v.id) : o?.id
                onChange && onChange({ target: { name: name, value: value } }, value)
            }}
            renderInput={(params) => (
                <TextField
                    required={required}
                    id={`input-${id}`}
                    name={`input-${id}`}
                    {...params}
                    label={label}
                    InputProps={{
                        ...params.InputProps,
                        endAdornment: (
                            <Box>
                                {loading ? <CircularProgress color="inherit" size={20} /> : null}
                                {params.InputProps.endAdornment}
                            </Box>
                        ),
                    }}
                />
            )}
        />
    )
}
export default ItemAutocomplete;