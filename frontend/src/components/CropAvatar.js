import { LoadingButton } from '@mui/lab'
import { Box, Button } from '@mui/material'
import React, { useRef, useState } from 'react'
import ReactCrop, { centerCrop, makeAspectCrop } from 'react-image-crop'
import 'react-image-crop/dist/ReactCrop.css'

export default function CropAvatar({
    src,
    handleClose,
    handleSet
}) {
    const [imgSrc] = useState(src)
    const imageRef = useRef(null)
    const [crop, setCrop] = useState()
    const [completedCrop, setCompletedCrop] = useState()
    const aspectRatio = 1
    const [loading, setLoading] = useState(false)

    const centerAspectCrop = (mediaWidth, mediaHeight, aspect) => centerCrop(
        makeAspectCrop(
            {
                unit: '%',
                width: 90,
            },
            aspect,
            mediaWidth,
            mediaHeight,
        ),
        mediaWidth,
        mediaHeight,
    )

    const onImageLoad = ({ currentTarget }) => {
        const { width, height } = currentTarget
        setCrop(centerAspectCrop(width, height, aspectRatio))
    }

    const dataURItoBlob = (dataURI) => {
        var byteString;
        if (dataURI.split(',')[0].indexOf('base64') >= 0) {
            byteString = atob(dataURI.split(',')[1]);
        } else {
            byteString = decodeURI(dataURI.split(',')[1]);
        }
        var type = dataURI.split(',')[0].split(':')[1].split(';')[0];
        var ia = new Uint8Array(byteString.length);
        for (var i = 0; i < byteString.length; i++) {
            ia[i] = byteString.charCodeAt(i);
        }
        return new Blob([ia], { type });
    }

    return (
        <Box>
            <Box mb={2}>
                <ReactCrop
                    crop={crop}
                    onChange={(_, percentCrop) => setCrop(percentCrop)}
                    onComplete={setCompletedCrop}
                    aspect={aspectRatio}
                >
                    <img
                        ref={imageRef}
                        alt=""
                        src={imgSrc}
                        onLoad={onImageLoad}
                    />
                </ReactCrop>
            </Box>
            <Box textAlign="center">
                <Button
                    size="large"
                    sx={{ mx: 2 }}
                    variant="outlined"
                    color="error"
                    onClick={handleClose}>Close</Button>
                <LoadingButton
                    size="large"
                    sx={{ mx: 2 }}
                    variant="contained"
                    color="primary"
                    loading={loading}
                    onClick={() => {
                        setLoading(true)
                        const canvas = document.createElement("canvas");

                        // get the image element
                        const image = imageRef.current;

                        // draw the image on the canvas
                        if (image) {
                            const crop = completedCrop;
                            const scaleX = image.naturalWidth / image.width;
                            const scaleY = image.naturalHeight / image.height;
                            const ctx = canvas.getContext("2d");
                            const pixelRatio = window.devicePixelRatio;
                            canvas.width = crop.width * pixelRatio * scaleX;
                            canvas.height = crop.height * pixelRatio * scaleY;

                            if (ctx) {
                                ctx.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
                                ctx.imageSmoothingQuality = "high";

                                ctx.drawImage(
                                    image,
                                    crop.x * scaleX,
                                    crop.y * scaleY,
                                    crop.width * scaleX,
                                    crop.height * scaleY,
                                    0,
                                    0,
                                    crop.width * scaleX,
                                    crop.height * scaleY
                                );
                            }

                            const base64Image = canvas.toDataURL("image/jpeg");
                            const blob = dataURItoBlob(base64Image);
                            if (blob.size > 4 * 1024 * 1024) {
                                setLoading(false)
                            } else {
                                handleSet(blob)
                            }
                        } else {
                            setLoading(false)
                        }
                    }}>Set</LoadingButton>
            </Box>
        </Box>
    )
}
