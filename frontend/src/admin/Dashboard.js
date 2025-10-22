import { CalendarMonth, Event, EventAvailable, HowToReg, EventNote, ThumbUpAlt, ThumbUp, Upcoming, People, EventAvailableSharp, EventBusy } from "@mui/icons-material";
import { Box, Card, CardContent, Grid, Typography } from '@mui/material';
import PeopleAltIcon from '@mui/icons-material/PeopleAlt';
import React, { useEffect, useState } from 'react';
import { Link } from "react-router-dom";
import fetcher from "../utils/fetcher";


export default function Dashboard() {
    const [data, setData] = useState();

    useEffect(() => {
        fetcher('/api/user/dashboard')
            .then(response => response.json())
            .then(data => setData(data.dashboard))
            .catch(error => console.error('Error fetching dashboard data:', error));
    }, [])

    return (
        <Grid container spacing={2} sx={{ p: 1 }}>
            <Grid size={{ xs: 12 }}>
                <Card sx={{ px: 2, display: "flex", justifyContent: 'space-between' }}>
                    <CardContent>
                        <Typography fontSize={{ md: 20, xs: 16 }}>Welcome</Typography>
                        <Typography fontWeight={700} fontSize={{ md: 24, xs: 18 }}>My Invite Admin</Typography>
                    </CardContent>
                </Card>
            </Grid>

            {/* Total Users */}
            <Grid size={{ xs: 12, md: 4 }}>
                <Card sx={{ height: 120, borderRadius: "4px", borderBottom: "4px solid blue" }}>
                    <Link to={"/admin/users"} style={{ textDecoration: 'none', color: 'inherit' }}>
                        <CardContent>
                            <Typography variant="h5" color="textSecondary">TOTAL USERS</Typography>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box display="flex" alignItems="center" gap={1} mt={1}>
                                    <PeopleAltIcon sx={{ fontSize: 20 }} />
                                    <Typography variant="h2" fontWeight={700}>{data?.total_users}</Typography>
                                </Box>
                                <People sx={{ fontSize: 50, opacity: 0.2, mt: 1 }} />
                            </Box>
                        </CardContent>
                    </Link>
                </Card>
            </Grid>

            {/* Total Events */}
            <Grid size={{ xs: 12, md: 4 }}>
                <Card sx={{ height: 120, borderRadius: "4px", borderBottom: "4px solid green" }}>
                    <Link to={"/admin/all-events"} style={{ textDecoration: 'none', color: 'inherit' }}>
                        <CardContent>
                            <Typography variant="h5" color="textSecondary">TOTAL EVENTS</Typography>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box display="flex" alignItems="center" gap={1} mt={1}>
                                    <Event sx={{ fontSize: 20 }} />
                                    <Typography variant="h2" fontWeight={700}>{data?.total_events}</Typography>
                                </Box>
                                <Event sx={{ fontSize: 50, opacity: 0.2, mt: 1 }} />
                            </Box>
                        </CardContent>
                    </Link>
                </Card>
            </Grid>

            {/* Top Engaged Event */}
            <Grid size={{ xs: 12, md: 4 }}>
                <Card sx={{ height: 120, borderRadius: "4px", borderBottom: "4px solid violet" }}>
                    <CardContent>
                        <Typography variant="h5" color="textSecondary">USERS JOINED THIS MONTH</Typography>
                        <Box display="flex" alignItems="center" justifyContent="space-between">
                            <Box display="flex" alignItems="center" gap={1} mt={1}>
                                <ThumbUp sx={{ fontSize: 20 }} />
                                <Typography variant="h2" fontWeight={700}>{data?.users_joined_this_month}</Typography>
                            </Box>
                            <ThumbUpAlt sx={{ fontSize: 50, opacity: 0.2, mt: 1 }} />
                        </Box>
                    </CardContent>
                </Card>
            </Grid>

            {/* Events This Month */}
            <Grid size={{ xs: 12, md: 4 }}>
                <Card sx={{ height: 120, borderRadius: "4px", borderBottom: "4px solid yellow" }}>
                    <CardContent>
                        <Typography variant="h5" color="textSecondary">EVENTS CREATED THIS MONTH</Typography>
                        <Box display="flex" alignItems="center" justifyContent="space-between">
                            <Box display="flex" alignItems="center" gap={1} mt={1}>
                                <CalendarMonth sx={{ fontSize: 20 }} />
                                <Typography variant="h2" fontWeight={700}>{data?.events_created_this_month}</Typography>
                            </Box>
                            <CalendarMonth sx={{ fontSize: 50, opacity: 0.2, mt: 1 }} />
                        </Box>
                    </CardContent>
                </Card>
            </Grid>

            {/* Upcoming Events */}
            <Grid size={{ xs: 12, md: 4 }}>
                <Card sx={{ height: 120, borderRadius: "4px", borderBottom: "4px solid red" }}>
                    <Link to={"/admin/upcoming-events"} style={{ textDecoration: 'none', color: 'inherit' }}>
                        <CardContent>
                            <Typography variant="h5" color="textSecondary">UPCOMING EVENTS</Typography>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box display="flex" alignItems="center" gap={1} mt={1}>
                                    <Upcoming sx={{ fontSize: 20 }} />
                                    <Typography variant="h2" fontWeight={700}>{data?.upcoming_events}</Typography>
                                </Box>
                                <Upcoming sx={{ fontSize: 50, opacity: 0.2, mt: 1 }} />
                            </Box>
                        </CardContent>
                    </Link>
                </Card>
            </Grid>

            {/* Completed Events */}
            <Grid size={{ xs: 12, md: 4 }}>
                <Card sx={{ height: 120, borderRadius: "4px", borderBottom: "4px solid orange" }}>
                    <Link to={"/admin/completed-events"} style={{ textDecoration: 'none', color: 'inherit' }}>
                        <CardContent>
                            <Typography variant="h5" color="textSecondary">COMPLETED EVENTS</Typography>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box display="flex" alignItems="center" gap={1} mt={1}>
                                    <EventAvailable sx={{ fontSize: 20 }} />
                                    <Typography variant="h2" fontWeight={700}>{data?.completed_events}</Typography>
                                </Box>
                                <EventAvailable sx={{ fontSize: 50, opacity: 0.2, mt: 1 }} />
                            </Box>
                        </CardContent>
                    </Link>
                </Card>
            </Grid>
        </Grid>

    )
}
