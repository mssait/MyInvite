import { BudgetType } from "../admin/BudgetType";
import Dashboard from "../admin/Dashboard";
import { AllEvents, Events } from "../admin/AllEvents";
import { EventType } from "../admin/EventType";
import menuItems from "../admin/menuItems";
import { Users } from "../admin/Users";
import MainLayout from "../layout/MainLayout";
import { UpcomingEvents } from "../admin/UpcomingEvents";
import { CompletedEvents } from "../admin/CompletedEvents";

const AdminRoutes = {
    path: '/admin',
    element: <MainLayout menuItems={menuItems} />,
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
            element: <UpcomingEvents/>
        },
        {
            path: 'completed-events',
            element: <CompletedEvents />
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