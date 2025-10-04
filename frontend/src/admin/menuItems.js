import { IconBrandMastercard, IconDashboard, IconTypography } from "@tabler/icons-react"

const menuItems = {
    items: [
        {
            id: 'home',
            title: 'Home',
            type: 'group',
            children: [
                {
                    id: 'view-dashboard',
                    title: 'Dashboard',
                    type: 'item',
                    url: '',
                    icon: IconDashboard
                },
                {
                    id: 'view-users',
                    title: 'Users',
                    type: 'item',
                    url: 'users',
                    icon: IconDashboard
                },
            ]
        },
        {
            id: 'events',
            title: 'Events',
            type: 'group',
            children: [
                {
                    id: 'view-all-events',
                    title: 'All Events',
                    type: 'item',
                    url: 'all-events',
                    icon: IconDashboard
                },
                {
                    id: 'view-upcoming-events',
                    title: 'Upcoming Events',
                    type: 'item',
                    url: 'upcoming-events',
                    icon: IconDashboard
                },
                {
                    id: 'view-completed-events',
                    title: 'Completed Events',
                    type: 'item',
                    url: 'completed-events',
                    icon: IconDashboard
                },
            ]
        },
        {
            id: 'master',
            title: 'Master',
            type: 'group',
            children: [
                {
                    id: 'view-eventTypes',
                    title: 'Event Types',
                    type: 'item',
                    url: 'event-types',
                    icon: IconBrandMastercard
                },
                {
                    id: 'view-budgetTypes',
                    title: 'Budget Types',
                    type: 'item',
                    url: 'budget-types',
                    icon: IconTypography
                }
            ]
        }
    ]
}

export default menuItems