import Login from "../auth/Login";
import Logout from "../auth/Logout";
import ForgotPassword from "../auth/ForgotPassword";
import MinimalLayout from "../layout/MinimalLayout";
import Event from "../admin/Event";
import { CommingSoon } from "../views/CommingSoon";



const DynamicRoutes = {
    path: '',
    element: <MinimalLayout />,
    children: [
        {
            path: '',
            element: <CommingSoon/>
        },
        {
            path: '/login',
            element: <Login />
        },
        {
            path: '/logout',
            element: <Logout />
        },
        {
            path: '/forgot-password',
            element: <ForgotPassword />
        },
        {
            path: '/event/:id',
            element: <Event />
        },
    ]
}
export default DynamicRoutes
