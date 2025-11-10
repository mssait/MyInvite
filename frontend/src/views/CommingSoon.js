import { Box, Typography } from '@mui/material'
import React from 'react'

export const CommingSoon = () => {
    return (
        <Box
            sx={{
                minHeight: "100vh",
                display: "flex",
                flexDirection: "column",
                justifyContent: "center",
                alignItems: "center",
                textAlign: "center",
                background: "linear-gradient(to bottom, #111, #222)",
                color: "white",
                px: 2,
            }}
        >
            <Typography
                variant="h1"
                sx={{ fontWeight: "bold", fontSize: "48px", mb: 20, letterSpacing: 1 }}
            >
                MY PERSONAL INVITE
            </Typography>

            <Typography
                variant="h1"
                sx={{
                    fontWeight: "bold",
                    fontSize: "46px",
                    mb: 2,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: 1,
                }}
            >
                🚀 Launching Soon
            </Typography>

            <Typography sx={{ opacity: 0.9, fontSize: "40px"}}>
                Owned &amp; Operated by Znifa Technologies Pvt Ltd.
            </Typography>
        </Box>)
}
