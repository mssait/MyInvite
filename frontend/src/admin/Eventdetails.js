
import { Box, Button, Card, CardContent, CardMedia, Grid, Grid2, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import fetcher from '../utils/fetcher'
import { IconCalendarMonth, IconLocation } from '@tabler/icons-react'

export const EventDetails = () => {
    const [eventDetails, setEventDetails] = useState([])

    useEffect(() => {
        fetcher('/api/event/details')
            .then(r => r.json())
            .then(result => {
                setEventDetails(result.eventDetails || [])
            })
    }, [])

    return (
        <Box sx={{ p: 2, bgcolor: '#f5e6ff', minHeight: '100vh' }}>
            {eventDetails?.map((event, index) => (
                <Grid2 size={{ xs: 12, md: 4 }} key={index}>
                    <Grid2 item xs={12}>
                        <Typography variant="h3" gutterBottom>Aarav's Birthday</Typography>
                        <Card sx={{ borderRadius: 2, boxShadow: 3 }}>
                            <CardMedia
                                component="img"
                                height="200"
                                image={event.thumbnails}
                                alt={event.title}
                            />
                            <CardContent>
                                <Typography variant="h5" gutterBottom>{event.title}</Typography>
                                <Box>
                                    <Box sx={{ backgroundColor: "#9e2ae1ff", borderRadius: "10%", padding: 0.4, display: "flex" }}>
                                        <IconCalendarMonth sx={{ color: "white", fontSize: 18 }} />
                                    </Box>
                                    <Typography variant="body3" ml={1}>{event.date}</Typography>
                                    <Typography variant="body3" ml={1}>{event.start_time - event.end_time}</Typography>
                                </Box>
                                <Box component="a" href={`https://www.google.com/maps?q=${event.location_latitude},${event.location_logitude}`} target="_blank" rel="noopener noreferrer" display="flex" alignItems="center" gap={1} mb={3} sx={{ textDecoration: "none", color: "white" }}>
                                    <Box sx={{ backgroundColor: "#9e2ae1ff", borderRadius: "10%", padding: 0.4, display: "flex" }}>
                                        <IconLocation sx={{ color: "white", fontSize: 18 }} />
                                    </Box>
                                    <Typography variant="body3" ml={1}>{event.address}</Typography>
                                </Box>
                                <Typography variant="h6" mt={2}>About Event</Typography>
                                <Typography variant="h6" mt={2}>{event.description}</Typography>
                                <Typography variant="body2">Carpooling</Typography>
                                <Button variant="contained" color="primary" sx={{ mt: 1 }}>
                                    View Offers
                                </Button>
                            </CardContent>
                        </Card>
                    </Grid2>

                </Grid2>
            ))}
        </Box>

    )
}




