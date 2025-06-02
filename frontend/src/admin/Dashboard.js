import { Box, Breadcrumbs, Card, CardActionArea, CardContent, CardHeader, Typography } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { BarChart } from '@mui/x-charts';
import { PieChart } from '@mui/x-charts/PieChart';
import React from 'react';


export default function Dashboard() {
    return (
        <Box>
            <Grid container spacing={2}>
                <Grid size={12}>
                    <Breadcrumbs>
                        <Typography variant="h3">
                            Home
                        </Typography>
                        <Typography variant="h3" color="text.primary">
                            Dashboard
                        </Typography>
                    </Breadcrumbs>
                </Grid>
                <Grid size={4}>
                    <Card>
                        <CardActionArea>
                            <CardHeader title="Best Company" subheader="Chennai" />
                            <CardContent>
                                <Typography variant="h1">
                                    Hion Studios
                                </Typography>
                            </CardContent>
                        </CardActionArea>
                    </Card>
                </Grid>
                <Grid size={4}>
                    <Card>
                        <CardActionArea>
                            <CardHeader title="Best Company" subheader="Chennai" />
                            <CardContent>
                                <Typography variant="h1">
                                    Hion Studios
                                </Typography>
                            </CardContent>
                        </CardActionArea>
                    </Card>
                </Grid>
                <Grid size={4}>
                    <Card>
                        <CardActionArea>
                            <CardHeader title="Best Company" subheader="Chennai" />
                            <CardContent>
                                <Typography variant="h1">
                                    Hion Studios
                                </Typography>
                            </CardContent>
                        </CardActionArea>
                    </Card>
                </Grid>
                <Grid size={4}>
                    <PieChart
                        series={[
                            {
                                arcLabel: item => item.value,
                                highlightScope: { highlight: 'item', fade: 'global' },
                                data: [
                                    { id: 0, value: 10, label: 'series A' },
                                    { id: 1, value: 15, label: 'series B' },
                                    { id: 2, value: 20, label: 'series C' },
                                ],
                            },
                        ]}
                        width={400}
                        height={200}
                    />
                </Grid>
                <Grid size={8}>
                    <BarChart
                        xAxis={[{ scaleType: 'band', data: ['group A', 'group B', 'group C'] }]}
                        series={[
                            { data: [4, 3, 5], highlightScope: { highlight: 'item', fade: 'global' } },
                            { data: [1, 6, 3], highlightScope: { highlight: 'item', fade: 'global' } },
                            { data: [2, 5, 6], highlightScope: { highlight: 'item', fade: 'global' } }
                        ]}
                        width={600}
                        height={300}
                    />
                </Grid>
            </Grid>
        </Box>
    )
}
