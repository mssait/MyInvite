import Login from "../auth/Login";
import Logout from "../auth/Logout";
import ForgotPassword from "../auth/ForgotPassword";
import MinimalLayout from "../layout/MinimalLayout";

const DynamicRoutes = {
    path: '',
    element: <MinimalLayout />,
    children: [
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
    ]
}
export default DynamicRoutes
