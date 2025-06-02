import { Box, Button, Card, CardContent, Grid, IconButton, Typography } from "@mui/material";
import { grey } from "@mui/material/colors";
import { IconPhotoUp, IconX } from "@tabler/icons-react";
import { useState } from "react";
import ImageUploading from "react-images-uploading";

export default function HionImageUpload({
    multiple = false,
    maxNumber = 1,
    acceptType = ["jpg", "jpeg", "png", "webp"],
    handleChange,
    handleRemove,
    name = "images",
    maxFileSize = 5 * 1024 * 1024, //5 MB
    buttonText = "Upload Image",
    paperElevation = 2,
    defaultImages = []
}) {
    const [images, setImages] = useState([]);
    const [defaultImage, setDefaultImage] = useState(defaultImages)

    const onChange = imageList => {
        setImages(imageList);
        if (handleChange) {
            const images = imageList.map(({ file }) => file)
            const value = multiple ? images : images?.[0]
            handleChange({ target: { name, value } }, value)
        }
    };

    return (
        <ImageUploading
            multiple={multiple}
            value={images}
            onChange={onChange}
            maxNumber={maxNumber}
            acceptType={acceptType}
            maxFileSize={maxFileSize}
        >
            {({
                imageList,
                onImageUpload,
                onImageRemove,
                isDragging,
                dragProps
            }) => (
                <Card
                    variant={isDragging ? "outlined" : "elevation"}
                    elevation={paperElevation}
                    sx={{
                        textAlign: 'center',
                        border: isDragging ? "2px solid #054192" : "none",
                    }}
                >
                    <CardContent>
                        <Box {...dragProps}>
                            <Box mb={2}>
                                <IconPhotoUp size={80} />
                            </Box>
                            <Grid container spacing={2} mb={2}>
                                {defaultImage.map((image, i) => (
                                    <Grid item md={4} xs={12} key={i} mx="auto">
                                        <Box position="relative">
                                            <img
                                                style={{
                                                    maxWidth: '100%',
                                                    height: 'auto'
                                                }}
                                                src={image}
                                                alt="" />
                                            <Box position="absolute" top={0} left={0}>
                                                <IconButton
                                                    sx={{ bgcolor: grey[300] }}
                                                    onClick={() => {
                                                        setDefaultImage(defaultImage => defaultImage.splice(0, i))
                                                        handleRemove && handleRemove(i)
                                                    }}>
                                                    <IconX />
                                                </IconButton>
                                            </Box>
                                        </Box>
                                    </Grid>
                                ))}
                                {imageList.map(({ dataURL }, i) => (
                                    <Grid item md={4} xs={12} key={i} mx="auto">
                                        <Box position="relative">
                                            <img style={{ maxWidth: '100%', height: 'auto' }} src={dataURL} alt="" />
                                            <Box position="absolute" top={0} left={0}>
                                                <IconButton
                                                    sx={{ bgcolor: grey[300] }}
                                                    onClick={() => {
                                                        handleRemove && handleRemove(i)
                                                        return onImageRemove(i)
                                                    }}>
                                                    <IconX />
                                                </IconButton>
                                            </Box>
                                        </Box>
                                    </Grid>
                                ))}
                            </Grid>
                            <Typography mb={1}>Accepted: {acceptType.join(', ')}</Typography>
                            {maxFileSize && (
                                <Typography mb={1}>Max Size: {maxFileSize / 1024 / 1024} MB</Typography>
                            )}
                            <Box>
                                <Button
                                    disabled={(!multiple && images.length === 1) || (multiple && images.length === maxNumber)}
                                    variant="contained"
                                    onClick={onImageUpload}
                                >{buttonText}</Button>
                            </Box>
                        </Box>
                    </CardContent>
                </Card>
            )}
        </ImageUploading>
    )
}
