import {
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  IconButton,
  Typography,
} from "@mui/material";
import { IconFile, IconPhotoUp, IconX } from "@tabler/icons-react";
import React, { useState } from "react";

export default function HionFileUpload({
  handleChange,
  maxFileSize = 5 * 1024 * 1024, // 5 MB
  buttonText = "Upload File",
  paperElevation = 2,
  multiple = false,
  max = 1,
  name
}) {
  const [value, setValue] = useState([]);

  const mime = ["image/png", "image/jpeg"];
  const onChange = async ({ target }) => {
    const files = Array.from(target.files);
    if (files.length > 0) {
      const fileList = [...value, ...files]
      setValue(fileList);
      handleChange?.({ target: { name, value: fileList } }, fileList);
    }
  };

  const handleRemoveFile = (index) => {
    const newValue = value.splice(0, index);
    setValue(newValue);
    handleChange?.({ target: { name, value: newValue } }, newValue);
  };

  return (
    <Card elevation={paperElevation}>
      <CardContent>
        <Grid container spacing={2} textAlign="center">
          <Grid item xs={12}>
            <IconPhotoUp size={80} />
          </Grid>
          <Grid item xs={12}>
            {maxFileSize && (
              <Typography>Max Size: {maxFileSize / 1024 / 1024} MB</Typography>
            )}
          </Grid>
          {value?.length > 0 &&
            value.map((file, index) => (
              <Grid item mx="auto" key={index}>
                <Box position="relative">
                  <Card variant="outlined">
                    <CardContent>
                      {mime.indexOf(file.type) > -1 ? (
                        <>
                          <img
                            style={{ maxWidth: "100px", height: "auto" }}
                            src={URL.createObjectURL(
                              new Blob([file], { type: file.type })
                            )}
                            alt=""
                          />
                        </>
                      ) : (
                        <>
                          <IconFile size={40} />
                          <Typography>Name: {file.name}</Typography>
                          <Typography>Type: {file.type}</Typography>
                          <Typography>
                            Size: {Math.round(file.size / 1024)} KB
                          </Typography>
                        </>
                      )}
                    </CardContent>
                  </Card>

                  <Box position="absolute" top={0} right={0}>
                    <IconButton
                      title="Remove file"
                      onClick={() => handleRemoveFile(index)}
                    >
                      <IconX />
                    </IconButton>
                  </Box>
                </Box>
              </Grid>
            ))}
          <Grid item xs={12}>
            <Button
              disabled={value.length >= max}
              component="label"
              variant="contained"
            >
              {buttonText}
              <input
                multiple={multiple}
                type="file"
                hidden
                name={name}
                onChange={(event) => {
                  onChange(event);
                }}
              />
            </Button>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
}
