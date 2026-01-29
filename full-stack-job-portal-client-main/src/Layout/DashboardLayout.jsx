import React, { createContext, useContext, useState } from "react";
import Wrapper from "../assets/css/wrappers/Dashboard";
import { Outlet, useNavigate } from "react-router-dom";

import { SmallSidebar, LargeSidebar, DashboardNavbar } from "../components";
import Swal from "sweetalert2";
import { useUserContext } from "../context/UserContext";
import axios from "axios";
import { buildApiUrl } from "../utils/FetchHandlers";

const DashboardContext = createContext();

const DashboardLayout = () => {
    const { handleFetchMe, user } = useUserContext();
    const [showSidebar, setShowSidebar] = useState(false);
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            const response = await axios.post(
                buildApiUrl(`/api/v1/auth/logout`),
                {},
                { withCredentials: true }
            );
            Swal.fire({
                icon: "success",
                title: "Logout...",
                text: response?.data?.message,
            });
            // Clear user state and redirect to home
            await handleFetchMe();
            navigate("/");
        } catch (error) {
            Swal.fire({
                icon: "error",
                title: "Oops...",
                text: error?.response?.data?.message || error?.message || "Logout failed",
            });
            // Still redirect even if there's an error
            navigate("/");
        }
    };

    // passing values
    const values = { handleLogout, showSidebar, setShowSidebar };
    return (
        <DashboardContext.Provider value={values}>
            <Wrapper>
                <main className="dashboard">
                    <SmallSidebar />
                    <LargeSidebar />
                    <div className="">
                        <DashboardNavbar />
                        <div className="dashboard-page">
                            <Outlet />
                        </div>
                    </div>
                </main>
            </Wrapper>
        </DashboardContext.Provider>
    );
};

export const useDashboardContext = () => useContext(DashboardContext);
export default DashboardLayout;
