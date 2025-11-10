
import React, { useEffect, useState } from "react";
import { Box, Container, Typography, Chip, Stack, Button, Paper, IconButton, Divider, Skeleton, Card, CardContent, Grid, AppBar, Toolbar } from "@mui/material";
import {
    IconCalendar, IconLocation, IconEdit, IconCheck, IconX, IconQuestionMark,
    IconUsers,
    IconUser,
} from "@tabler/icons-react";
import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import { useParams, useNavigate } from "react-router-dom";
import fetcher from "../utils/fetcher";
import { toImage, toDate, toTimeMillis, toTime } from "../utils/util";

// ---------- Utility Functions ----------
const keepLastAddressParts = (address, count = 3) => {
    if (!address) return "—";
    const parts = address.split(",").map((p) => p.trim()).filter(Boolean);
    return parts.length <= count ? address : parts.slice(-count).join(", ");
};

const Navbar = () => (
    <AppBar
        position="fixed"
        sx={{
            backgroundColor: "#373643",
            boxShadow: "none",
            backdropFilter: "blur(6px)",
        }}
    >
        <Toolbar
            sx={{
                display: "flex",
                justifyContent: "space-between", // logo left, icon right
                alignItems: "center",
                py: 1,
                px: { xs: 2, md: 4 },
            }}
        >
            {/* Logo on the left */}
            <Box
                component="img"
                src="/logo.png"
                alt="Logo"
                sx={{
                    height: { xs: 40, md: 60 },
                    width: "auto",
                    objectFit: "contain",
                    cursor: "pointer",
                    filter: "drop-shadow(0 0 4px rgba(0,0,0,0.5))",
                }}
            />
            <IconButton
                color="inherit"
                sx={{
                    color: "white"
                }}
            >
                <IconUser size={30} />
            </IconButton>
        </Toolbar>
    </AppBar>
);

// ---------- Hero Carousel ----------
// ---------- Hero Carousel (NO ARROWS) ----------
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
        arrows: false,
        beforeChange: (_, next) => setActiveIndex(next),
        appendDots: (dots) => (
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
                {dots}
            </Box>
        ),
        customPaging: (i) => (
            <Box
                sx={{
                    width: activeIndex === i ? 24 : 8,
                    height: 8,
                    borderRadius: 1,
                    bgcolor: activeIndex === i ? "#FF6B6B" : "rgba(255,255,255,0.7)",
                    transition: "all 0.3s ease",
                }}
            />
        ),
    };

    if (!images?.length) {
        return <Skeleton variant="rectangular" height={400} />;
    }

    return (
        <Box
            sx={{
                width: "100%",
                backgroundColor: "black",
                overflow: "hidden",
                position: "relative",
            }}
        >
            <Slider {...settings}>
                {images.map((src, i) => (
                    <Box key={i} sx={{ position: "relative" }}>
                        <Box
                            component="img"
                            src={src}
                            alt={`Event image ${i + 1}`}
                            sx={{
                                width: "100%",
                                height: { xs: 300, md: 400 },
                                objectFit: "cover",
                                display: "block",
                            }}
                        />
                    </Box>
                ))}
            </Slider>

            <Box
                sx={{
                    position: "absolute",
                    inset: 0,
                    background: "linear-gradient(to top, rgba(0,0,0,0.4), transparent)",
                }}
            />
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

    const PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.yourcompany.yourapp";

    const openPlayStore = () => {
        window.open(PLAY_STORE_URL, "_blank", "noopener,noreferrer");
        // still call the parent handler (if you want to keep UI sync)
        onStatusChange?.(status);
    };

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

        <Grid
            container
            spacing={2}
            mt={2}
        >
            {/* Attending Button */}
            <Grid size={5} textAlign="center">
                <Button
                    variant="contained"
                    disabled={loading}
                    fullWidth
                    size="large"
                    sx={{
                        bgcolor: "#4CAF50",
                        color: "white",
                        borderRadius: 0.5,
                        "&:hover": { bgcolor: "#388E3C" },
                    }}
                    onClick={openPlayStore}
                >
                    {loading ? "..." : "Attending"}
                </Button>
            </Grid>

            {/* No Button */}
            <Grid size={3.5} textAlign="center">
                <Button
                    variant="outlined"
                    disabled={loading}
                    size="large"
                    fullWidth
                    sx={{
                        borderColor: "#F44336",
                        color: "#F44336",
                        borderRadius: 0.5
                    }}
                    onClick={openPlayStore}
                >
                    No
                </Button>
            </Grid>

            {/* Maybe Button */}
            <Grid size={3.5} textAlign="center">
                <Button
                    variant="outlined"
                    disabled={loading}
                    size="large"
                    fullWidth
                    sx={{
                        borderColor: "#FF9800",
                        color: "#FF9800",
                        borderRadius: 0.5
                    }}
                    onClick={openPlayStore}
                >
                    Maybe
                </Button>
            </Grid>
        </Grid>

    );
};

// ---------- PlayStore App Bar ----------
const PlayStoreBar = () => {
    return (
        <Box
            sx={{
                my: 4,
                py: 3,
                bgcolor: "grey.100",
                borderRadius: 3,
                textAlign: "center",
            }}
        >
            <Typography variant="h6" fontWeight={600} gutterBottom>
                Get the app on Google Play
            </Typography>
            <Box
                component="a"
                href="https://play.google.com/store/apps/details?id=your.app.id"
                target="_blank"
                rel="noopener"
                sx={{ display: "inline-block" }}
            >
                <img
                    src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"
                    alt="Get it on Google Play"
                    height="80"
                />
            </Box>
        </Box>
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
                setEvent(data.EventDetails[0]);
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
                <Typography textAlign='center' color="error">Failed to load event details.</Typography>
            </Container>
        );
    }

    const images = event.thumbnails?.length > 0
        ? event.thumbnails.map(t => toImage(t.image))
        : ["/Event_thumbnail1.png", "/Event_thumbnail2.png", "/Event_thumbnail3.png"];


    return (
        <Box>
            {/* Hero Carousel */}
            <Navbar />
            <Box sx={{ mt: { xs: 7, md: 9 } }}>
                <HeroCarousel images={images} />
                <Container
                    maxWidth="md"
                    sx={{
                        py: { xs: 4, md: 6 },
                        px: { xs: 2, md: 4 },
                        display: "flex",
                        flexDirection: "column",
                        gap: { xs: 3, md: 5 },
                    }}
                >
                    <Stack spacing={{ xs: 3, md: 5 }}>
                        {/* Title */}
                        <Typography
                            variant="h2"
                            fontWeight={700}
                            sx={{
                                fontSize: { xs: "1.6rem", md: "2.5rem" },
                                textAlign: { xs: "left", md: "center" },
                            }}
                        >
                            {event.title || "Untitled Event"}
                        </Typography>

                        {/* Guests */}
                        <Stack
                            direction={{ xs: "row", md: "row" }}
                            spacing={1.5}
                            alignItems="center"
                            justifyContent={{ xs: "flex-start", md: "center" }}
                        >
                            <Box
                                sx={{
                                    bgcolor: "secondary.main",
                                    borderRadius: 1,
                                    p: 1,
                                    display: "flex",
                                }}
                            >
                                <IconUsers color="white" size={22} />
                            </Box>
                            {event.no_of_guest > 0 && (
                                <Typography
                                    variant="body1"
                                    fontWeight={500}
                                    sx={{ fontSize: { xs: "1rem", md: "1.1rem" } }}
                                >
                                    <strong>Total Guests: {event.no_of_guest}</strong>
                                </Typography>
                            )}
                        </Stack>

                        {/* Attendance Buttons */}
                        <Box
                        >
                            <AttendanceButtons
                                status={rsvpStatus}
                                onStatusChange={handleRsvpChange}
                                loading={rsvpLoading}
                            />
                        </Box>

                        <Divider sx={{ my: { xs: 2, md: 4 } }} />

                        {/* Play Store Bar */}
                        <Box sx={{ mx: { xs: 0, md: "auto" }, width: { xs: "100%", md: "100%" } }}>
                            <PlayStoreBar />
                        </Box>

                        <Divider sx={{ my: { xs: 2, md: 4 } }} />

                        {/* Date & Location Grid */}
                        <Box
                            sx={{
                                display: "grid",
                                gridTemplateColumns: {
                                    xs: "1fr",
                                    md: "1fr 1fr",
                                },
                                gap: { xs: 2, md: 4 },
                                alignItems: "start",
                            }}
                        >
                            {/* Date & Time */}
                            <Stack direction="row" spacing={2} alignItems="center">
                                <Box
                                    sx={{
                                        bgcolor: "secondary.main",
                                        borderRadius: 1,
                                        p: 1,
                                        display: "flex",
                                    }}
                                >
                                    <IconCalendar color="white" size={22} />
                                </Box>
                                <Box>
                                    <Typography variant="body1" fontWeight={600}>
                                        {event.date ? toDate(event.date) : "—"}
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        {event.start_time && event.end_time
                                            ? `${toTime(event.start_time)} - ${toTime(event.end_time)}`
                                            : "—"}
                                    </Typography>
                                </Box>
                            </Stack>

                            {/* Location */}
                            <Stack
                                direction="row"
                                spacing={2}
                                alignItems="center"
                                component="a"
                                href={`https://www.google.com/maps?q=${event.location_latitude},${event.location_longitude}`}
                                target="_blank"
                                rel="noopener noreferrer"
                                sx={{
                                    textDecoration: "none",
                                    color: "inherit",
                                    cursor: "pointer",
                                    "&:hover": { color: "primary.main" },
                                }}
                            >
                                <Box
                                    sx={{
                                        bgcolor: "secondary.main",
                                        borderRadius: 1,
                                        p: 1,
                                        display: "flex",
                                    }}
                                >
                                    <IconLocation color="white" size={22} />
                                </Box>
                                <Typography
                                    variant="body1"
                                    fontWeight={600}
                                    sx={{
                                        lineHeight: 1.4,
                                        maxWidth: { xs: "100%", md: "90%" },
                                        wordBreak: "break-word",
                                    }}
                                >
                                    {event?.address}
                                </Typography>
                            </Stack>
                        </Box>

                        <Divider sx={{ my: { xs: 2, md: 4 } }} />

                        {/* About Event */}
                        <Box>
                            <Typography
                                variant="h3"
                                fontWeight={600}
                                gutterBottom
                                sx={{ fontSize: { xs: "1.5rem", md: "2rem" }, textAlign: { md: "center" } }}
                            >
                                About Event
                            </Typography>
                            <Typography
                                variant="body1"
                                color="text.secondary"
                                sx={{
                                    lineHeight: 1.7,
                                    textAlign: { xs: "left", md: "center" },
                                    fontSize: { xs: "0.95rem", md: "1.05rem" },
                                }}
                            >
                                {event.description?.trim() ||
                                    "It is a long established fact that a reader will be distracted by readable content."}
                            </Typography>
                        </Box>

                        {/* Carpooling Banner */}
                        <Paper
                            elevation={4}
                            sx={{
                                borderRadius: 3,
                                overflow: "hidden",
                                position: "relative",
                                height: { xs: 140, sm: 200, md: 320, lg: 380 },
                                mt: 3,
                                backgroundImage: "url(/carpool_banner.png)",
                                backgroundSize: "cover",
                                backgroundPosition: "center",
                                display: "flex",
                                alignItems: "center",
                            }}
                        >
                            {/* Gradient overlay for text readability */}
                            <Box
                                sx={{
                                    position: "absolute",
                                    inset: 0,
                                    background: {
                                        xs: "linear-gradient(90deg, rgba(0,0,0,0.45), rgba(0,0,0,0.15), transparent)",
                                        md: "linear-gradient(90deg, rgba(0,0,0,0.5), rgba(0,0,0,0.25), transparent)",
                                    },
                                }}
                            />

                            {/* Content Box */}
                            <Box
                                sx={{
                                    position: "relative",
                                    p: { xs: 2, sm: 3, md: 6, lg: 8 },
                                    color: "white",
                                    maxWidth: { xs: "100%", md: "50%" }, // prevent text from stretching too far
                                }}
                            >
                                <Typography
                                    sx={{
                                        fontWeight: 700,
                                        fontSize: { xs: 14, sm: 18, md: 36, lg: 44 },
                                        lineHeight: 1.2,
                                    }}
                                >
                                    GOT EMPTY SEATS!
                                </Typography>

                                <Typography
                                    sx={{
                                        mt: { xs: 0.5, md: 1.5 },
                                        opacity: 0.95,
                                        fontSize: { xs: 11, sm: 14, md: 22, lg: 26 },
                                        lineHeight: 1.3,
                                    }}
                                >
                                    Share your ride, cut costs & CO₂!
                                </Typography>

                                <Button
                                    variant="contained"
                                    sx={{
                                        mt: { xs: 1.5, sm: 2, md: 3 },
                                        bgcolor: "white",
                                        color: "black",
                                        textTransform: "none",
                                        fontWeight: 600,
                                        fontSize: { xs: 10, sm: 12, md: 16 },
                                        px: { xs: 2, md: 4 },
                                        height: { xs: 28, sm: 34, md: 44 },
                                        "&:hover": {
                                            bgcolor: "#f5f5f5",
                                        },
                                    }}
                                    href="https://play.google.com/store/apps/details?id=your.app.id"
                                    target="_blank"
                                    rel="noopener"
                                >
                                    Offer Carpool
                                </Button>
                            </Box>
                        </Paper>

                    </Stack>
                </Container>
            </Box>

        </Box>
    );
}