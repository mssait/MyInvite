import { IconDashboard } from "@tabler/icons-react"

const menuItems = {
    items: [
        {
            id: 'dashboard',
            title: 'Dashboard',
            type: 'group',
            children: [
                {
                    id: 'view-dashboard',
                    title: 'Dashboard',
                    type: 'item',
                    url: '',
                    icon: IconDashboard
                }
            ]
        }
    ]
}

export default menuItems