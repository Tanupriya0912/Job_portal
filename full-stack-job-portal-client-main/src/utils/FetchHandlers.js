import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const buildApiUrl = (path) => {
    if (path.startsWith('http')) return path;
    return `${API_BASE_URL}${path}`;
};

export const getAllHandler = async (url) => {
    const res = await axios.get(buildApiUrl(url), { withCredentials: true });
    return res.data;
};

export const getSingleHandler = async (url) => {
    const res = await axios.get(buildApiUrl(url), { withCredentials: true });
    return res?.data?.result;
};

export const postHandler = async ({ url, body }) => {
    return await axios.post(buildApiUrl(url), body, { withCredentials: true });
};

export const updateHandler = async ({ url, body }) => {
    const res = await axios.patch(buildApiUrl(url), body, { withCredentials: true });
    return res?.data;
};

export const updateHandlerPut = async ({ url, body }) => {
    return await axios.put(buildApiUrl(url), body, { withCredentials: true });
};

export const deleteHandler = async (url) => {
    return await axios.delete(buildApiUrl(url), { withCredentials: true });
};

/**
 * Role validation utilities
 */

/**
 * Check if user has required role
 * @param {Object} user - User object with role property
 * @param {string|Array} requiredRole - Single role string or array of allowed roles
 * @returns {boolean} True if user has required role
 */
export const hasRole = (user, requiredRole) => {
    if (!user || !user.role) return false;
    
    if (Array.isArray(requiredRole)) {
        return requiredRole.includes(user.role);
    }
    
    return user.role === requiredRole;
};

/**
 * Check if user is recruiter
 * @param {Object} user - User object with role property
 * @returns {boolean} True if user role is recruiter
 */
export const isRecruiter = (user) => {
    return user?.role === "recruiter";
};

/**
 * Check if user is admin
 * @param {Object} user - User object with role property
 * @returns {boolean} True if user role is admin
 */
export const isAdmin = (user) => {
    return user?.role === "admin";
};

/**
 * Check if user is regular user/candidate
 * @param {Object} user - User object with role property
 * @returns {boolean} True if user role is user
 */
export const isCandidate = (user) => {
    return user?.role === "user";
};

/**
 * Get role-based error message
 * @param {string} actionName - Name of the action (e.g., "create jobs", "manage users")
 * @param {string} requiredRole - Required role for the action
 * @returns {string} Error message
 */
export const getRoleErrorMessage = (actionName, requiredRole) => {
    return `You do not have permission to ${actionName}. Only ${requiredRole}s can perform this action.`;
};
