import Dashboard from "../admin/Dashboard";
import menuItems from "../admin/menuItems";
import MainLayout from "../layout/MainLayout";

const AdminRoutes = {
    path: '/admin',
    element: <MainLayout menuItems={menuItems} />,
    children: [
        {
            path: '',
            element: <Dashboard />
        }
    ]
}

export default AdminRoutes