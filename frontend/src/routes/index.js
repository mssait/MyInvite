import { useRoutes } from 'react-router-dom';
import DynamicRoutes from "./DynamicRoutes";
import AdminRoutes from './AdminRoutes';

export default function ThemeRoutes() {
    return useRoutes([DynamicRoutes, AdminRoutes]);
}
