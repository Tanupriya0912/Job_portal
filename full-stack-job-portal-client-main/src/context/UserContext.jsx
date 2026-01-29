import React, { useContext, useEffect, useState } from "react";
import axios from "axios";
import { buildApiUrl } from "../utils/FetchHandlers";

const userContext = React.createContext();

const UserContext = ({ children }) => {
    const [userLoading, setUserLoading] = useState(true);
    const [userError, setUserError] = useState({ status: false, message: "" });
    const [user, setUser] = useState(null);

    const handleFetchMe = async () => {
        setUserLoading(true);
        try {
            const response = await axios.get(buildApiUrl('/api/v1/auth/me'), {
                withCredentials: true,
            });
            setUserError({ status: false, message: "" });
            // Ensure role is included in user state
            const userData = response?.data?.result || {};
            // Backend returns Role enum (e.g. "RECRUITER"). Normalize to lowercase
            if (userData && userData.role) {
                try {
                    userData.role = String(userData.role).toLowerCase();
                } catch (e) {
                    // ignore
                }
            }
            setUser(userData);
        } catch (error) {
            const message = error?.response?.data?.message || error?.message || "Failed to fetch user";
            setUserError({ status: true, message });
            setUser(null);
        }
        setUserLoading(false);
    };

    useEffect(() => {
        handleFetchMe();
    }, []);

    const passing = { userLoading, userError, user, handleFetchMe };
    return (
        <userContext.Provider value={passing}>{children}</userContext.Provider>
    );
};

const useUserContext = () => useContext(userContext);

export { useUserContext, UserContext };
