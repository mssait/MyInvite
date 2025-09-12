import { Box, Breadcrumbs, Button, Card, CardContent, Chip, IconButton, Stack, Tooltip, Typography } from '@mui/material';
import { orange, purple, red, teal } from '@mui/material/colors';
import { GridToolbarContainer, GridToolbarExport, GridToolbarFilterButton } from '@mui/x-data-grid-premium';
import { IconEdit, IconPlus } from '@tabler/icons-react';
// import { useConfirm } from 'material-ui-confirm';
import { useSnackbar } from 'notistack';
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import ClientDataGrid from '../components/ClientDataGrid';
import fetcher from '../utils/fetcher';


export const AllEvents = () => {
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

    return (
        <Stack spacing={2}>
            <Card>
                <CardContent>
                    <Box display={{ md: "flex", xs: "column" }} justifyContent="space-between" alignItems="center">
                        <Box>
                            <Typography variant="h3" pb={{ md: 0, xs: 1 }}>Events</Typography>
                        </Box>
                        <Box sx={{ width: { xs: "80%", md: "auto" } }}>
                            <Breadcrumbs maxItems={2} separator="›">
                                <Typography variant="h4" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>Admin</Typography>
                                <Typography variant="h4" color="text.primary" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
                                    Events
                                </Typography>
                            </Breadcrumbs>
                        </Box>
                    </Box>
                </CardContent>
            </Card>
            <ClientDataGrid
                Toolbar={AddToolbar}
                refresh={refresh}
                ajax={{ url: "/api/event/all" }}
                columns={[
                    {
                        headerName: "Action",
                        field: "Action",
                        width: "250",
                        id: "Action",
                        type: "actions",
                        getActions: ({ id }) => (
                            [
                                 <Chip label="View Details" component={Link} to={`${id}/event-details`} key={id} sx={{ bgcolor: purple[100], color: purple[900], borderRadius: "8px" }} />
                            ]
                        )
                    },
                    {
                        headerName: "Title",
                        field: "title",
                        width: "200",
                        id: "title",
                        type: "string",                       
                    },
                    {
                        headerName: "Host Name",
                        field: "name",
                        width: "200",
                        id: "name",
                        type: "string",
                    },
                    {
                        headerName: "Event Type",
                        field: "type",
                        width: "200",
                        id: "type",
                        type: "string",
                    },
                    {
                        headerName: "Date",
                        field: "date",
                        width: "200",
                        id: "date",
                        type: "date",
                    },
                    {
                        headerName: "location",
                        field: "location",
                        width: "200",
                        id: "location",
                        type: "string",
                    },
                    
                    
                    
                ]}
            />
        </Stack>
    )
}
