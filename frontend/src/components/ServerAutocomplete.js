import { Autocomplete, Box, CircularProgress, TextField } from "@mui/material";
import { useEffect, useState } from "react";
import fetcher from "../utils/fetcher";

const ServerAutocomplete = (
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
        filterSelectedOptions,
        disabled,
        helperText,
        server
    }) => {

    const [open, setOpen] = useState(false);
    const [options, setOptions] = useState([])
    const loading = open && options.length === 0;
    const [selectValue, setSelectValue] = useState(multiple ? [] : null);

    useEffect(() => {
        fetcher(`/api/select/${select}`)
            .then(res => res.json())
            .then(data => {
                if (value) {
                    if (multiple) {
                        setSelectValue(data.options?.filter(({ id }) => value.indexOf(id) > -1))
                    } else {
                        setSelectValue(data.options?.filter(({ id }) => id === value)[0])
                    }
                }
                setOptions(data.options)
            })
            .catch(console.log)
    }, [])

    const params = {}
    if (server) {
        params.filterOptions = x => x;
    }

    return (
        <Autocomplete
            name={name}
            multiple={multiple}
            disabled={disabled}
            filterSelectedOptions={filterSelectedOptions}
            id={id}
            open={open}
            onOpen={() => setOpen(true)}
            onClose={() => setOpen(false)}
            getOptionLabel={option => option?.label}
            options={options}
            loading={loading}
            onBlur={() => {
                const value = multiple ? selectValue && selectValue.map(({ id }) => id) : selectValue?.id
                onBlur && onBlur({ target: { name, value } })
            }}
            value={selectValue}
            onChange={(_, o) => {
                setSelectValue(o)
                const value = multiple ? o?.map(({ id }) => id) : o?.id
                onChange && onChange({ target: { name, value } }, value)
            }}
            renderInput={params => (
                <TextField
                    required={required}
                    id={`input-${id}`}
                    name={`input-${id}`}
                    {...params}
                    label={label}
                    helperText={helperText}
                    InputProps={{
                        ...params.InputProps,
                        endAdornment: (
                            <Box>
                                {loading ? <CircularProgress color="inherit" size={20} /> : null}
                                {params.InputProps.endAdornment}
                            </Box>
                        )
                    }}
                />
            )}
            {...params}
        />
    )
}
export default ServerAutocomplete;