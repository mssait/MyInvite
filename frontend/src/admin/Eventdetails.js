
import { Box, Breadcrumbs, Button, Card, CardContent, Grid, Skeleton, Stack, Typography } from '@mui/material'
import { IconCalendarMonth, IconLocation } from '@tabler/icons-react'
import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import BreadcrumbLink from '../components/BreadcrumbLink'
import fetcher from '../utils/fetcher'
import { toDate, toImage, toTimeMillis } from '../utils/util'

export const EventDetails = ({ id = useParams()["id"] }) => {
    const [eventDetails, setEventDetails] = useState(null);
    const location = useLocation();
    useEffect(() => {
        fetcher(`/api/event/${id}/details`)
            .then(r => r.json())
            .then(result => {
                setEventDetails(result.EventDetails || [])
            })
    }, [])

    const RenderBreadcrumbs  = () => {
        if (location.pathname.includes("all-events")) {
            return (
                <Breadcrumbs maxItems={2} separator="›">
                    <Typography variant="h4" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>Admin</Typography>
                    <BreadcrumbLink to={'/admin/all-events'}>All Events</BreadcrumbLink>
                    <Typography variant="h4" color="text.primary" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
                        Details
                    </Typography>
                </Breadcrumbs>
            );
        } else if (location.pathname.includes("upcoming-events")) {
            return (
                <Breadcrumbs maxItems={2} separator="›">
                    <Typography variant="h4" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>Admin</Typography>
                    <BreadcrumbLink to={'/admin/upcoming-events'}>Upcoming Events</BreadcrumbLink>
                    <Typography variant="h4" color="text.primary" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
                        Details
                    </Typography>
                </Breadcrumbs>
            );
        } else if (location.pathname.includes("completed-events")) {
            return (
                <Breadcrumbs maxItems={2} separator="›">
                    <Typography variant="h4" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>Admin</Typography>
                    <BreadcrumbLink to={'/admin/completed-events'}>Completed Events</BreadcrumbLink>
                    <Typography variant="h4" color="text.primary" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
                        Details
                    </Typography>
                </Breadcrumbs>
            );
        }
        return null;
    }

    return (
        <Stack spacing={2}>
            <Card>
                <CardContent>
                    <Box display={{ md: "flex", xs: "column" }} justifyContent="space-between" alignItems="center">
                        <Box>
                            <Typography variant="h3" pb={{ md: 0, xs: 1 }}>Event Details</Typography>
                        </Box>
                        <Box sx={{ width: { xs: "80%", md: "auto" } }}>
                            {RenderBreadcrumbs ()}
                        </Box>
                    </Box>
                </CardContent>
            </Card>

            {!Array.isArray(eventDetails) ? (
                <Skeleton variant="rounded" height={"500px"} />
            ) : (
                <Box>
                    {eventDetails?.map((event, index) => (
                        <Grid size={{ xs: 12, md: 12 }} key={index}>
                            <Card sx={{ borderRadius: 2, display: "flex", flexDirection: { xs: "column", md: "row" }, overflow: "hidden" }}>

                                {/* Left Side - Content */}
                                <CardContent sx={{ flex: 1, p: 3 }}>
                                    <Typography variant="h1" mb={3}>{event.title}</Typography>

                                    <Box display="flex" alignItems="center" gap={2} mb={2}>
                                        <Box sx={{ backgroundColor: "secondary.main", borderRadius: "10%", padding: 0.8, display: "flex" }}>
                                            <IconCalendarMonth color="#373643" sx={{ fontSize: 18 }} />
                                        </Box>
                                        <Typography variant="body3">{toDate(event.date)}</Typography>
                                        <Typography variant="body3">{toTimeMillis(event.start_time)} - {toTimeMillis(event.end_time)}</Typography>
                                    </Box>

                                    <Box component="a"
                                        href={`https://www.google.com/maps?q=${event.location_latitude},${event.location_longitude}`}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        display="flex"
                                        alignItems="center"
                                        gap={1}
                                        mb={4}
                                        sx={{ textDecoration: "none", color: "text.primary" }}>
                                        <Box sx={{ backgroundColor: "secondary.main", borderRadius: "10%", padding: 0.8, display: "flex" }}>
                                            <IconLocation color="#373643" sx={{ fontSize: 18 }} />
                                        </Box>
                                        <Typography variant="body3" ml={1}>{event.address}</Typography>
                                    </Box>

                                    <Typography variant="h2" mt={2}>About Event</Typography>
                                    <Typography variant="h4" mt={3}>Description</Typography>
                                    <Typography mt={1}>{event?.description || "No description"}</Typography>

                                    <Typography variant="h4" mt={3}>No. of Guests</Typography>
                                    <Typography mt={1}>{event.no_of_guest}</Typography>

                                    <Typography variant="h4" mt={3}>Gift Suggestion</Typography>
                                    <Typography mt={1}>{event.gift_suggestion}</Typography>

                                    {/* <Button
                                        variant="contained"
                                        color="primary"
                                        size="small"
                                        component={Link}
                                        to={`carpools`}
                                        sx={{ mt: 3 }}>
                                        View Carpools
                                    </Button> */}
                                </CardContent>

                                {/* Right Side - Image Collage */}
                                <Box sx={{
                                    flex: 1,
                                    display: "grid",
                                    gridTemplateColumns: "repeat(auto-fit, minmax(150px, 1fr))",
                                    gap: 1,
                                    p: 1,
                                    alignContent: "flex-start",
                                }}>
                                    {event.thumbnails.map((thumb, idx) => (
                                        <Box
                                            key={idx}
                                            component="img"
                                            src={toImage(thumb.image)}
                                            alt={`${event.title}-${idx}`}
                                            sx={{
                                                width: "100%",
                                                height: 250,
                                                objectFit: "cover",
                                                borderRadius: 1,
                                            }}
                                        />
                                    ))}
                                </Box>

                            </Card>
                        </Grid>
                    ))}
                </Box>

            )}
        </Stack>
    )
}




