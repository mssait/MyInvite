import { Box, Breadcrumbs, Button, Card, CardContent, Chip, IconButton, Stack, Tooltip, Typography } from '@mui/material';
import { orange, red, teal } from '@mui/material/colors';
import { GridToolbarContainer, GridToolbarExport, GridToolbarFilterButton } from '@mui/x-data-grid-premium';
import { IconEdit, IconPlus } from '@tabler/icons-react';
// import { useConfirm } from 'material-ui-confirm';
import { useSnackbar } from 'notistack';
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import ClientDataGrid from '../components/ClientDataGrid';
import fetcher from '../utils/fetcher';

export const Users = () => {
    // const confirm = useConfirm()
    const { enqueueSnackbar } = useSnackbar()
    const [refresh, setRefresh] = useState(0)

    const AddToolbar = () => {
        return (
            <GridToolbarContainer>
                <Box sx={{ mt: 1, ml: 1 }}>
                    <GridToolbarFilterButton />
                </Box>
                <Box sx={{ mt: 1, ml: 1 }}>
                    <GridToolbarExport />
                </Box>
            </GridToolbarContainer>
        );
    };

    // const handleInactive = async (id) => {
    //     const res = await fetcher(`/api/admin/audition/${id}`, { method: 'put' })
    //     const { status, message } = await res.json();
    //     if (status === 'success') {
    //         enqueueSnackbar('Marked as Inactive', { variant: 'success' });
    //         setRefresh(refresh => refresh + 1);
    //     } else {
    //         enqueueSnackbar(message, { variant: 'error' });
    //     }
    // }

    // const handleClick = (id) => {
    //     confirm({ description: (<Typography variant="body2">Once marked as inactive, this item cannot be edited or shown on the website. Are you sure you want to continue?</Typography>) })
    //         .then((result) => {
    //             if (result.confirmed) {
    //                 handleInactive(id);
    //             } else {
    //                 enqueueSnackbar("Action Cancelled", { variant: "success" });
    //             }
    //         })
    // };

    return (
        <Stack spacing={2}>
            <Card>
                <CardContent>
                    <Box display={{ md: "flex", xs: "column" }} justifyContent="space-between" alignItems="center">
                        <Box>
                            <Typography variant="h3" pb={{ md: 0, xs: 1 }}>Users</Typography>
                        </Box>
                        <Box sx={{ width: { xs: "80%", md: "auto" } }}>
                            <Breadcrumbs maxItems={2} separator="›">
                                <Typography variant="h4" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>Admin</Typography>
                                <Typography variant="h4" color="text.primary" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
                                    Users
                                </Typography>
                            </Breadcrumbs>
                        </Box>
                    </Box>
                </CardContent>
            </Card>
            <ClientDataGrid
                Toolbar={AddToolbar}
                refresh={refresh}
                ajax={{ url: "/api/user/view" }}
                columns={[
                    {
                        headerName: "Name",
                        field: "name",
                        width: "200",
                        id: "name",
                        type: "string",                       
                    },
                    {
                        headerName: "Email Id",
                        field: "email",
                        width: "200",
                        id: "email",
                        type: "email",
                    },
                    {
                        headerName: "Phone Number",
                        field: "phone_number",
                        width: "200",
                        id: "phone_number",
                        type: "phone",
                    },
                    {
                        headerName: "Profile",
                        field: "avatar",
                        width: "200",
                        id: "avatar",
                        type: "avatar",
                    },
                    
                    
                ]}
            />
        </Stack>
    )
}

