import { BudgetType } from "../admin/BudgetType";
import Dashboard from "../admin/Dashboard";
import { AllEvents, Events } from "../admin/AllEvents";
import { EventType } from "../admin/EventType";
import menuItems from "../admin/menuItems";
import { Users } from "../admin/Users";
import MainLayout from "../layout/MainLayout";
import { UpcomingEvents } from "../admin/UpcomingEvents";
import { CompletedEvents } from "../admin/CompletedEvents";
import AdminLayout from "../admin/AdminLayout";
import { ThemeProvider } from "@mui/material";
import theme from "../themes/theme";
import { EventDetails } from "../admin/Eventdetails";
import { EventCarpools } from "../admin/EventCarpools";




const AdminRoutes = {
    path: '/admin',
    element: (
        <ThemeProvider theme={theme}>
            <AdminLayout />
        </ThemeProvider>
    ),
    children: [
        {
            path: '',
            element: <Dashboard />
        },
        {
            path: 'users',
            element: <Users />
        },
        {
            path: 'all-events',
            element: <AllEvents />
        },
        {
            path: 'upcoming-events',
            element: <UpcomingEvents />
        },
        {
            path: 'completed-events',
            element: <CompletedEvents />
        },
        {
            path: 'completed-events/:id/details',
            element: <EventDetails />
        },
        {
            path: 'upcoming-events/:id/details',
            element: <EventDetails />
        },
        {
            path: 'all-events/:id/details',
            element: <EventDetails />
        },
        {
            path: 'all-events/:id/details/carpools',
            element: <EventCarpools />
        },
        {
            path: 'event-types',
            element: <EventType />
        },
        {
            path: 'budget-types',
            element: <BudgetType />
        },
    ]
}

export default AdminRoutes