import { Link as Href } from '@mui/material'
import React from 'react'
import { Link } from 'react-router-dom'

export default function BreadcrumbLink({ children, to }) {
    return (
        <Href component={Link} to={to} variant="h4" color='textSecondary' underline="hover" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
            {children}
        </Href>
    )
}