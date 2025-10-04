import { lazy } from "react";
import { Navigate } from "react-router-dom";
import { isAdmin, isLoggedIn, isOrgUser } from "../auth/AuthProvider";
import Loadable from "../components/Loadable";
import menuItems from "./menuItems";


const MainLayout = Loadable(lazy(() => import("../layout/MainLayout")));

const AdminLayout = () => {
    return isAdmin() && isLoggedIn() ? (
        <MainLayout menuItems={menuItems} />
    ) : (
        <Navigate to="/" />
    )
}

export default AdminLayout