import { Navigate } from "react-router-dom";
import { logout } from "./AuthProvider";

const Logout = () => {
    logout()
    return <Navigate to="/" />
}
export default Logout;