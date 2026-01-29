/* eslint-disable react/prop-types */

import styled from "styled-components";
import Logo from "../Logo";
import { NavLink, useNavigate } from "react-router-dom";
import { useUserContext } from "../../context/UserContext";
import { useDashboardContext } from "../../Layout/DashboardLayout";
import axios from "axios";
import { buildApiUrl } from "../../utils/FetchHandlers";
import Swal from "sweetalert2";

const Navbar = ({ navbarRef }) => {
    const navigate = useNavigate();
    const { user, handleFetchMe } = useUserContext();
    // handleLogout is provided by DashboardLayout; guard in case Navbar is used outside
    const dashContext = useDashboardContext();
    const handleLogout = dashContext?.handleLogout;

    const doLogout = async () => {
        try {
            // If we have the dashboard context, use it
            if (handleLogout) {
                await handleLogout();
            } else {
                // Otherwise, call logout directly
                const response = await axios.post(
                    buildApiUrl(`/api/v1/auth/logout`),
                    {},
                    { withCredentials: true }
                );
                Swal.fire({
                    icon: "success",
                    title: "Logout...",
                    text: response?.data?.message || "Logged out successfully",
                });
                // Clear user state
                await handleFetchMe();
            }
            // Navigate to home
            navigate("/");
        } catch (error) {
            Swal.fire({
                icon: "error",
                title: "Oops...",
                text: error?.response?.data?.message || error?.message || "Logout failed",
            });
            // Still navigate even if there's an error
            navigate("/");
        }
    };

    return (
        <Wrapper ref={navbarRef}>
            <div className="container">
                <Logo />
                <div className="flex justify-end items-center">
                    <NavLink className="nav-item" to="/all-jobs">
                        Jobs
                    </NavLink>
                    <NavLink className="nav-item" to="/about">
                        About
                    </NavLink>
                    <NavLink className="nav-item hidden sm:block" to="/dashboard">
                        Dashboard
                    </NavLink>
                    {!user?.email ? (
                        <NavLink className="nav-item" to="/login">
                            <span className="bg-[#247BF7] text-white px-6 py-2 rounded"> Login</span>
                        </NavLink>
                    ) : (
                        <button className="nav-item" onClick={doLogout}>
                            <span className="bg-[#247BF7] text-white px-6 py-2 rounded"> Logout</span>
                        </button>
                    )}
                </div>
            </div>
        </Wrapper>
    );
};

const Wrapper = styled.div`
    width: 100%;
    display: flex;
    justify-content: center;
    box-shadow: 0 5px 5px var(--shadow-light);
    padding: 1rem 0;
    .container {
        width: 100%;
        max-width: 1200px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .container .nav-item {
        font-size: 16px;
        font-weight: 500;
        text-transform: capitalize;
        margin-left: 20px;
        color: var(--color-black);
    }
    .container .nav-item.active {
        color: var(--color-primary);
    }
    @media screen and (max-width: 1200px) {
        padding: 1rem 2rem;
    }
    @media screen and (max-width: 600px) {
        padding: 1.2rem 1rem;
        .container {
            display: flex;
            /* justify-content: center; */
        }
    }
`;

export default Navbar;
