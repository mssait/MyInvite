import { Navigate } from "react-router-dom";
import { useLocation } from "react-router-dom";
import { isLoggedIn } from "../auth/AuthProvider";

const AuthPage = ({ children }) => {
    const { pathname } = useLocation()
    return isLoggedIn() ? children : <Navigate to={`/login?ref=${pathname}`} />
}
export default AuthPage