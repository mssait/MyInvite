import React from "react";
import { Box, Card, Typography, Avatar, Divider, Stack, CardContent } from "@mui/material";
import { IconCalendarMonth, IconCircle } from "@tabler/icons-react";
import BreadcrumbLink from "../components/BreadcrumbLink";
import { useLocation, useParams } from "react-router-dom";

const carpools = [
  {
    name: "Rahul Sharma",
    from: "Anna Nagar, Chennai",
    to: "Mamallapuram, Chennai",
    date: "26 June 2025",
    avatar: "https://i.pravatar.cc/100?img=3"
  },
  {
    name: "Rahul Sharma",
    from: "Mylapore, Chennai",
    to: "Saidapet, Chennai",
    date: "26 June 2025",
    avatar: "https://i.pravatar.cc/100?img=4"
  },
  {
    name: "Rahul Sharma",
    from: "Kalapatti, CBE",
    to: "Gandhipuram, CBE",
    date: "26 June 2025",
    avatar: "https://i.pravatar.cc/100?img=5"
  }
];

export const EventCarpools = ({ id = useParams()["id"] }) => {
  const location = useLocation();

  const Breadcrumbs = () => {
    if (location.pathname.includes("all-events")) {
      return (
        <Breadcrumbs maxItems={2} separator="›">
          <Typography variant="h4" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>Admin</Typography>
          <BreadcrumbLink to={'/admin/all-events'}>All Events</BreadcrumbLink>
          <BreadcrumbLink to={`/admin/all-events/${id}/details`}>Details</BreadcrumbLink>
          <Typography variant="h4" color="text.primary" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
            Carpools
          </Typography>
        </Breadcrumbs>
      );
    } else if (location.pathname.includes("upcoming-events")) {
      return (
        <Breadcrumbs maxItems={2} separator="›">
          <Typography variant="h4" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>Admin</Typography>
          <BreadcrumbLink to={'/admin/upcoming-events'}>Upcoming Events</BreadcrumbLink>
          <BreadcrumbLink to={`/admin/upcoming-events/${id}/details`}>Details</BreadcrumbLink>
          <Typography variant="h4" color="text.primary" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
            Carpools
          </Typography>
        </Breadcrumbs>
      );
    } else if (location.pathname.includes("completed-events")) {
      return (
        <Breadcrumbs maxItems={2} separator="›">
          <Typography variant="h4" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>Admin</Typography>
          <BreadcrumbLink to={'/admin/completed-events'}>Completed Events</BreadcrumbLink>
          <BreadcrumbLink to={`/admin/completed-events/${id}/details`}>Details</BreadcrumbLink>
          <Typography variant="h4" color="text.primary" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
            Carpools
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
              {Breadcrumbs()}
            </Box>
          </Box>
        </CardContent>
      </Card>
      <Box sx={{ minHeight: "100vh" }}>
        {carpools.map((carpool, index) => (
          <Card
            key={index}
            sx={{
              display: "flex",
              alignItems: "center",
              borderRadius: 1,
              p: 2,
              mb: 2,
            }}
          >
            {/* Left - Avatar + Name */}
            <Box
              sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                width: 120,
                pr: 2
              }}
            >
              <Avatar
                src={carpool.avatar}
                alt={carpool.name}
                sx={{ width: 70, height: 70, mb: 1 }}
              />
              <Typography
                variant="body1"
                sx={{ fontWeight: 600, color: "#373643", textAlign: "center" }}
              >
                {carpool.name}
              </Typography>
            </Box>

            <Divider orientation="vertical" flexItem sx={{ mx: 2 }} />

            {/* Right - Route & Date */}
            <Box sx={{ flex: 1 }}>
              <Box display="flex" alignItems="center" mb={1}>
                <IconCircle color="red" sx={{ fontSize: 10 }} />
                <Typography variant="body2" ml={1}>{carpool.from}</Typography>
              </Box>

              <Box display="flex" alignItems="center" mb={1}>
                <IconCircle color="green" sx={{ fontSize: 10 }} />
                <Typography variant="body2" ml={1}>{carpool.to}</Typography>
              </Box>

              <Box display="flex" alignItems="center" mt={1}>
                <IconCalendarMonth sx={{ fontSize: 16 }} />
                <Typography variant="body2" ml={1}>{carpool.date}</Typography>
              </Box>
            </Box>
          </Card>
        ))}
      </Box>
    </Stack>
  )
}
