import Cookies from 'js-cookie';
import config from '../config';
export const hasRole = (userRoles, roles) => userRoles.filter(role => roles.indexOf(role) > -1).length > 0
export const roleCheck = (userRoles, roles) => !roles || hasRole(userRoles, roles)
export const getAuth = () => Cookies.get(config.cookieName)
export const getRoles = () => JSON.parse(localStorage.roles || '[]')
export const logout = () => {
    clearAuth()
    clearAuthLocalStorage()
}
export const clearAuthLocalStorage = () => {
    localStorage.removeItem('name')
    localStorage.removeItem('email')
    localStorage.removeItem('type')
    localStorage.removeItem('roles')
}
export const isLoggedIn = () => Boolean(getAuth())
export const getUserType = () => localStorage.type;
export const isAdmin = () =>  getUserType() === 'User_admin';

export const clearAuth = () => {
    Cookies.remove(config.cookieName)
}
export const getHomePage = () => {
    switch (getUserType()) {
        case 'User_admin':
            return '/admin'
        default:
            return '/'
    }
}
export const getName = () => localStorage.getItem('name')
export const getEmail = () => localStorage.getItem('email')
export const getAvatar = () => localStorage.getItem('avatar')