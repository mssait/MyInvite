// Usereventdetails.jsx
import React, { useEffect, useState } from "react";
import {
    Box,
    Container,
    Typography,
    Chip,
    Stack,
    Button,
    Paper,
    IconButton,
    Divider,
    Skeleton,
    Card,
    CardContent,
    Breadcrumbs,
} from "@mui/material";
import {
    IconCalendar,
    IconLocation,
    IconCar,
    IconEdit,
    IconCheck,
    IconX,
    IconQuestionMark,
} from "@tabler/icons-react";
import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import { useParams, useNavigate } from "react-router-dom";
import BreadcrumbLink from "../components/BreadcrumbLink"; // Adjust path
import fetcher from "../utils/fetcher";
import { toImage, toDate, toTimeMillis } from "../utils/util";

// ---------- Utility Functions ----------
const keepLastAddressParts = (address, count = 3) => {
    if (!address) return "—";
    const parts = address.split(",").map((p) => p.trim()).filter(Boolean);
    return parts.length <= count ? address : parts.slice(-count).join(", ");
};

// ---------- Hero Carousel ----------
const HeroCarousel = ({ images }) => {
    const [activeIndex, setActiveIndex] = useState(0);

    const settings = {
        dots: false,
        infinite: true,
        autoplay: true,
        autoplaySpeed: 5000,
        speed: 800,
        slidesToShow: 1,
        slidesToScroll: 1,
        beforeChange: (_, next) => setActiveIndex(next),
    };

    if (!images || images.length === 0) {
        return <Skeleton variant="rectangular" height={400} />;
    }

    return (
        <Box position="relative">
            <Slider {...settings}>
                {images.map((img, i) => (
                    <Box key={i}>
                        <Box
                            component="img"
                            src={toImage(img)}
                            alt={`Event image ${i + 1}`}
                            sx={{
                                width: "100%",
                                height: { xs: 300, md: 400 },
                                objectFit: "cover",
                            }}
                        />
                    </Box>
                ))}
            </Slider>

            {/* Gradient Overlay */}
            <Box
                sx={{
                    position: "absolute",
                    inset: 0,
                    background: "linear-gradient(to top, rgba(0,0,0,0.35), transparent)",
                }}
            />

            {/* Dots Indicator */}
            <Box
                sx={{
                    position: "absolute",
                    bottom: 16,
                    left: 0,
                    right: 0,
                    display: "flex",
                    justifyContent: "center",
                    gap: 1,
                }}
            >
                {images.map((_, i) => (
                    <Box
                        key={i}
                        sx={{
                            width: activeIndex === i ? 24 : 8,
                            height: 8,
                            borderRadius: 1,
                            bgcolor: activeIndex === i ? "#FF6B6B" : "rgba(255,255,255,0.7)",
                            transition: "all 0.3s ease",
                        }}
                    />
                ))}
            </Box>
        </Box>
    );
};

const AttendanceButtons = ({ status, onStatusChange, loading }) => {
    const [editing, setEditing] = useState(false);

    const statusConfig = {
        1: { label: "Count me in", color: "#4CAF50", icon: <IconCheck /> },
        2: { label: "You’re sitting this one out", color: "#F44336", icon: <IconX /> },
        3: { label: "You’re on the “Maybe” list", color: "#FF9800", icon: <IconQuestionMark /> },
    };

    const current = status ? statusConfig[status] : null;

    if (loading) {
        return <Skeleton height={60} width="100%" />;
    }

    if (!editing && current) {
        return (
            <Stack direction="row" alignItems="center" spacing={1} mt={2}>
                <Typography variant="subtitle1" sx={{ color: current.color, fontWeight: 600 }}>
                    {current.label}
                </Typography>
                {current.icon}
                <IconButton size="small" onClick={() => setEditing(true)}>
                    <IconEdit size={18} />
                </IconButton>
            </Stack>
        );
    }

    return (
        <Stack direction={{ xs: "row", sm: "column" }} spacing={1.5} mt={2}>
            <Button
                variant="contained"
                disabled={loading}
                sx={{
                    bgcolor: "#4CAF50",
                    color: "white",
                    minWidth: 100,
                    height: 48,
                    "&:hover": { bgcolor: "#388E3C" },
                }}
                onClick={() => { onStatusChange(1); setEditing(false); }}
            >
                {loading ? "..." : "Attending"}
            </Button>
            <Button
                variant="outlined"
                disabled={loading}
                sx={{
                    borderColor: "#F44336",
                    color: "#F44336",
                    minWidth: 80,
                    height: 48,
                }}
                onClick={() => { onStatusChange(2); setEditing(false); }}
            >
                No
            </Button>
            <Button
                variant="outlined"
                disabled={loading}
                sx={{
                    borderColor: "#FF9800",
                    color: "#FF9800",
                    minWidth: 80,
                    height: 48,
                }}
                onClick={() => { onStatusChange(3); setEditing(false); }}
            >
                Maybe
            </Button>
        </Stack>
    );
};


export default function Event() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [event, setEvent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [rsvpStatus, setRsvpStatus] = useState(null);
    const [rsvpLoading, setRsvpLoading] = useState(false);


    useEffect(() => {
        const fetchEvent = async () => {
            try {
                setLoading(true);
                const res = await fetcher(`/api/event/${id}/details`);
                const data = await res.json();
                setEvent(data);
                setRsvpStatus(data.rsvpStatusId || null);
            } catch (err) {
                console.error("Failed to fetch event:", err);
                setEvent(null);
            } finally {
                setLoading(false);
            }
        };
        fetchEvent();
    }, [id]);


    const handleRsvpChange = async (newStatus) => {
        setRsvpLoading(true);
        try {
            await fetcher(`/api/event/${id}/rsvp`, {
                method: "POST",
                body: JSON.stringify({ rsvpStatusId: newStatus }),
            });
            setRsvpStatus(newStatus);
        } catch (err) {
            console.error("RSVP failed:", err);
            alert("Failed to update RSVP. Please try again.");
        } finally {
            setRsvpLoading(false);
        }
    };

    // Loading State
    if (loading) {
        return (
            <Container sx={{ py: 4 }}>
                <Stack spacing={3}>
                    <Card>
                        <CardContent>
                            <Skeleton height={60} width="60%" />
                        </CardContent>
                    </Card>
                    <Skeleton variant="rectangular" height={400} />
                    <Skeleton height={100} />
                    <Skeleton height={100} />
                    <Skeleton height={100} />
                </Stack>
            </Container>
        );
    }

    // Error State
    if (!event) {
        return (
            <Container sx={{ py: 4 }}>
                <Typography color="error">Failed to load event details.</Typography>
            </Container>
        );
    }

    const images = event.imageIds?.length > 0
        ? event.imageIds.map(toImage)
        : ["/assets/images/1.jpg", "/assets/images/2.jpg"];

    const venue = keepLastAddressParts(event.address, 3);
    const eventId = event.eventId || parseInt(id);

    return (
        <Box>
            {/* Hero Carousel */}
            <HeroCarousel images={images} />

            <Container maxWidth="md" sx={{ py: 4 }}>
                <Stack spacing={3}>
                    {/* Header Card */}
                    <Card>
                        <CardContent>
                            <Box
                                display={{ md: "flex", xs: "column" }}
                                justifyContent="space-between"
                                alignItems="center"
                            >
                                <Typography variant="h3" pb={{ md: 0, xs: 1 }}>
                                    Event Details
                                </Typography>
                            
                            </Box>
                        </CardContent>
                    </Card>

                    {/* Title */}
                    <Typography variant="h4" fontWeight={700}>
                        {event.title || "Untitled Event"}
                    </Typography>

                    {/* Tags + Invited */}
                    <Stack direction="row" spacing={2} alignItems="center">
                        <Chip
                            icon={<IconCalendar size={16} />}
                            label="Event"
                            size="small"
                            sx={{
                                bgcolor: "secondary.main",
                                color: "white",
                                "& .MuiChip-icon": { color: "white" },
                            }}
                        />
                        {event.invitedCount > 0 && (
                            <Typography variant="body2" color="text.secondary">
                                <strong>{event.invitedCount} invited</strong>
                            </Typography>
                        )}
                    </Stack>

                    {/* Attendance Buttons */}
                    <AttendanceButtons
                        status={rsvpStatus}
                        onStatusChange={handleRsvpChange}
                        loading={rsvpLoading}
                    />

                    <Divider />

                    {/* Date & Time */}
                    <Stack direction="row" spacing={2} alignItems="center">
                        <Box
                            sx={{
                                bgcolor: "secondary.main",
                                borderRadius: 2,
                                p: 1,
                                display: "flex",
                            }}
                        >
                            <IconCalendar color="white" size={20} />
                        </Box>
                        <Box>
                            <Typography variant="subtitle1" fontWeight={600}>
                                {event.startTime ? toDate(event.startTime) : "—"}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                {event.startTime && event.endTime
                                    ? `${toTimeMillis(event.startTime)} - ${toTimeMillis(event.endTime)}`
                                    : "—"}
                            </Typography>
                        </Box>
                    </Stack>

                    {/* Location */}
                    <Stack direction="row" spacing={2} alignItems="center">
                        <Box
                            sx={{
                                bgcolor: "secondary.main",
                                borderRadius: 2,
                                p: 1,
                                display: "flex",
                            }}
                        >
                            <IconLocation color="white" size={20} />
                        </Box>
                        <Typography variant="subtitle1" fontWeight={600}>
                            {venue}
                        </Typography>
                    </Stack>

                    <Divider />

                    {/* About Event */}
                    <Box>
                        <Typography variant="h6" fontWeight={600} gutterBottom>
                            About Event
                        </Typography>
                        <Typography variant="body1" color="text.secondary" sx={{ lineHeight: 1.6 }}>
                            {event.description?.trim() ||
                                "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout."}
                        </Typography>
                    </Box>

                    {/* Carpooling Banner */}
                    {eventId && (
                        <Paper
                            elevation={3}
                            sx={{
                                borderRadius: 2,
                                overflow: "hidden",
                                position: "relative",
                                height: 140,
                                mt: 3,
                                backgroundImage: "url(/assets/images/carpooling.png)",
                                backgroundSize: "cover",
                                backgroundPosition: "center",
                            }}
                        >
                            <Box
                                sx={{
                                    position: "absolute",
                                    inset: 0,
                                    background: "linear-gradient(90deg, rgba(0,0,0,0.45), rgba(0,0,0,0.15), transparent)",
                                }}
                            />
                            <Stack
                                direction="row"
                                justifyContent="space-between"
                                alignItems="center"
                                sx={{ position: "relative", p: 2, height: "100%" }}
                            >
                                <Box>
                                    <Typography variant="h6" sx={{ color: "white", fontWeight: 700 }}>
                                        GOT EMPTY SEATS!
                                    </Typography>
                                    <Typography variant="body2" sx={{ color: "white", opacity: 0.95, mt: 0.5 }}>
                                        Share your ride, cut costs & CO₂!
                                    </Typography>
                                    <Button
                                        size="small"
                                        variant="contained"
                                        sx={{
                                            mt: 1.5,
                                            bgcolor: "white",
                                            color: "black",
                                            fontSize: 10,
                                            height: 28,
                                            px: 1.5,
                                            textTransform: "none",
                                        }}
                                        onClick={() => navigate(`/createCarpool/${eventId}`)}
                                    >
                                        Offer Carpool
                                    </Button>
                                </Box>
                            </Stack>
                        </Paper>
                    )}

                    {/* View Carpools CTA */}
                    {eventId && (
                        <Button
                            fullWidth
                            variant="contained"
                            size="large"
                            sx={{
                                mt: 2,
                                py: 1.8,
                                bgcolor: "#3B3B45",
                                "&:hover": { bgcolor: "#2f2f35" },
                                borderRadius: 2,
                                textTransform: "none",
                                fontWeight: 600,
                            }}
                            onClick={() => navigate(`/myCarpools/${eventId}`)}
                        >
                            View Carpooling Offers
                        </Button>
                    )}
                </Stack>
            </Container>
        </Box>
    );
}