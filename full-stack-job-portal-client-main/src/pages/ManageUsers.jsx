import React from "react";
import { useUserContext } from "../context/UserContext";
import LoadingComTwo from "../components/shared/LoadingComTwo";
import { CiSquarePlus } from "react-icons/ci";
import styled from "styled-components";

import Swal from "sweetalert2";
import { getAllHandler, buildApiUrl } from "../utils/FetchHandlers";
import { useQuery } from "@tanstack/react-query";
import axios from "axios";

const ManageUsers = () => {
    const { user: me } = useUserContext();
    const {
        isPending,
        isError,
        data: users,
        error,
        refetch,
    } = useQuery({
        queryKey: ["users"],
        queryFn: () =>
            getAllHandler(buildApiUrl('/api/v1/users')),
    });

    React.useEffect(() => {
        console.log("Users data:", users);
        if (users?.result && users.result.length > 0) {
            console.log("First user:", users.result[0]);
            console.log("First user _id:", users.result[0]._id);
            console.log("First user id:", users.result[0].id);
        }
    }, [users]);

    const updateUserModal = (id, role) => {
        Swal.fire({
            title: "Are you sure?",
            text: "You won't be able to revert this!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#19b74b",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes",
        }).then((result) => {
            if (result.isConfirmed) {
                UpdateUserRole(id, role);
            }
        });
    };

    const deleteUserModal = (id, username) => {
        console.log("Delete user modal called with id:", id, "username:", username);
        Swal.fire({
            title: "Delete User?",
            text: `You are about to delete user "${username}". This action cannot be undone!`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#d33",
            cancelButtonColor: "#19b74b",
            confirmButtonText: "Yes, Delete",
        }).then((result) => {
            if (result.isConfirmed) {
                DeleteUser(id);
            }
        });
    };

    const DeleteUser = async (id) => {
        try {
            const response = await axios.delete(
                buildApiUrl(`/api/v1/users/${id}`),
                { withCredentials: true }
            );
            await refetch();
            Swal.fire({
                title: "Deleted!",
                text: "User has been deleted successfully",
                icon: "success",
            }).then(() => {
                // Dialog closes, data is already refreshed
            });
        } catch (error) {
            console.log("Delete user error:", error);
            Swal.fire({
                title: "Sorry!",
                text: error?.response?.data?.message || "Failed to delete user",
                icon: "error",
            });
        }
    };

    const editUserModal = (user) => {
        Swal.fire({
            title: "Edit User",
            html: `
                <div style="text-align: left; display: flex; flex-direction: column; gap: 10px;">
                    <div>
                        <label style="display: block; font-weight: bold; margin-bottom: 5px;">Username</label>
                        <input type="text" id="username" value="${user.username}" disabled style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;"/>
                    </div>
                    <div>
                        <label style="display: block; font-weight: bold; margin-bottom: 5px;">Email</label>
                        <input type="email" id="email" value="${user.email}" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;"/>
                    </div>
                    <div>
                        <label style="display: block; font-weight: bold; margin-bottom: 5px;">Location</label>
                        <input type="text" id="location" value="${user.location || ''}" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;"/>
                    </div>
                    <div>
                        <label style="display: block; font-weight: bold; margin-bottom: 5px;">Gender</label>
                        <select id="gender" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                            <option value="male" ${user.gender === 'male' ? 'selected' : ''}>Male</option>
                            <option value="female" ${user.gender === 'female' ? 'selected' : ''}>Female</option>
                            <option value="other" ${user.gender === 'other' ? 'selected' : ''}>Other</option>
                        </select>
                    </div>
                </div>
            `,
            showCancelButton: true,
            confirmButtonText: "Update",
            confirmButtonColor: "#19b74b",
            cancelButtonColor: "#d33",
            allowOutsideClick: false,
            preConfirm: () => {
                const email = document.getElementById('email').value;
                const location = document.getElementById('location').value;
                const gender = document.getElementById('gender').value;
                
                if (!email) {
                    Swal.showValidationMessage('Email is required!');
                    return false;
                }
                
                UpdateUserProfile(user.id, { email, location, gender });
                return true;
            }
        });
    };

    const UpdateUserProfile = async (userId, updates) => {
        try {
            const response = await axios.patch(
                buildApiUrl(`/api/v1/users/${userId}`),
                updates,
                { withCredentials: true }
            );
            await refetch();
            Swal.fire({
                title: "Updated!",
                text: "User profile has been updated successfully",
                icon: "success",
            }).then(() => {
                // Dialog closes, data is already refreshed
            });
        } catch (error) {
            console.log("Update user error:", error);
            Swal.fire({
                title: "Sorry!",
                text: error?.response?.data?.message || "Failed to update user profile",
                icon: "error",
            });
        }
    };

    const UpdateUserRole = async (id, role) => {
        const updateUser = { role };
        try {
            const response = await axios.patch(
                buildApiUrl(`/api/v1/users/${id}/role`),
                updateUser,
                { withCredentials: true }
            );
            await refetch();
            Swal.fire({
                title: "Done!",
                text: "Role Updated Successfully",
                icon: "success",
            }).then(() => {
                // Dialog closes, data is already refreshed
            });
        } catch (error) {
            console.log("Update role error:", error);
            Swal.fire({
                title: "Sorry!",
                text: error?.response?.data?.message || "Failed to update role",
                icon: "error",
            });
        }
    };

    if (isPending) {
        return <LoadingComTwo />;
    }
    
    // Check for errors
    if (isError) {
        console.error("Error loading users:", error);
        return (
            <h2 className="text-lg md:text-3xl font-bold text-red-600 text-center mt-12">
                Error loading users: {error?.message || "Failed to fetch users"}
            </h2>
        );
    }

    if (users) {
        console.log("Users data:", users);
        console.log("Users result:", users?.result);
        console.log("Users result length:", users?.result?.length);
        console.log("Users type:", typeof users);
        console.log("Full users object:", JSON.stringify(users, null, 2));
    }

    if (!users || !users?.result || !users?.result?.length) {
        console.log("No users found. Users data:", users);
        return (
            <h2 className="text-lg md:text-3xl font-bold text-red-600 text-center mt-12">
                -- User List is Empty --
            </h2>
        );
    }
    return (
        <Wrapper>
            <div className="title-row">
                Manage Users
                <CiSquarePlus className="ml-1 text-xl md:text-2xl" />
            </div>
            <div className="content-row">
                <table className="table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Username</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users?.result?.map((user, index) => {
                            let i =
                                index + 1 < 10 ? `0${index + 1}` : index + 1;
                            return (
                                <tr key={user.id}>
                                    <td>{i}</td>
                                    <td>{user?.username}</td>
                                    <td>{user?.email}</td>
                                    <td>{user?.role}</td>
                                    <td className="action-row">
                                        {user?.id === me?._id ? null : (
                                            <>
                                                {" "}
                                                {String(user?.role).toLowerCase() ===
                                                "admin" ? null : (
                                                    <button
                                                        className="action admin"
                                                        onClick={() =>
                                                            updateUserModal(
                                                                user.id,
                                                                "admin"
                                                            )
                                                        }
                                                    >
                                                        admin
                                                    </button>
                                                )}
                                                {String(user?.role).toLowerCase() ===
                                                "recruiter" ? null : (
                                                    <button
                                                        className="action recruiter"
                                                        onClick={() =>
                                                            updateUserModal(
                                                                user.id,
                                                                "recruiter"
                                                            )
                                                        }
                                                    >
                                                        recuiter
                                                    </button>
                                                )}
                                                {String(user?.role).toLowerCase() ===
                                                "user" ? null : (
                                                    <button
                                                        className="action user"
                                                        onClick={() =>
                                                            updateUserModal(
                                                                user.id,
                                                                "user"
                                                            )
                                                        }
                                                    >
                                                        user
                                                    </button>
                                                )}
                                            </>
                                        )}
                                        <button
                                            className="action delete"
                                            onClick={() => {
                                                console.log("Delete button clicked for user:", user);
                                                deleteUserModal(
                                                    user.id,
                                                    user.username
                                                );
                                            }}
                                        >
                                            delete
                                        </button>
                                        <button
                                            className="action edit"
                                            onClick={() => {
                                                console.log("Edit button clicked for user:", user);
                                                editUserModal(user);
                                            }}
                                        >
                                            edit
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
        </Wrapper>
    );
};

const Wrapper = styled.section`
    .title-row {
        display: flex;
        justify-content: flex-start;
        align-items: center;
        font-size: calc(0.9rem + 0.4vw);
        text-transform: capitalize;
        letter-spacing: 1px;
        font-weight: 600;
        opacity: 0.85;
        color: var(--color-black);
        position: relative;
    }
    .title-row:before {
        content: "";
        position: absolute;
        bottom: -4px;
        left: 0;
        width: calc(30px + 0.7vw);
        height: calc(2px + 0.1vw);
        background-color: var(--color-primary);
    }
    .content-row {
        overflow-x: auto;
        margin-top: calc(2rem + 0.5vw);
    }
    .table {
        border-collapse: collapse;
        border-spacing: 0;
        width: 100%;
        border: 1px solid #ddd;
        border-radius: 8px;
    }
    .table thead {
        background-color: var(--color-accent);
        color: var(--color-white);
        font-size: 14px;
        letter-spacing: 1px;
        font-weight: 400;
        text-transform: capitalize;
    }

    .table th,
    .table td {
        text-align: left;
        padding: 12px;
    }

    .table tbody tr {
        font-size: 15px;
        font-weight: 400;
        text-transform: capitalize;
        letter-spacing: 1px;
        transition: all 0.2s linear;
    }

    .table tbody tr:nth-child(even) {
        background-color: #00000011;
    }

    .table .action-row {
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
        align-items: center;
        column-gap: 12px;
    }
    .table .action-row .action {
        font-size: 16px;
        padding: 1px 8px;
        border-radius: 4px;
        color: #fff;
        text-transform: capitalize;
    }
    .action.recruiter {
        background-color: #ac04ac;
    }
    .action.admin {
        background-color: #5f14c7;
    }
    .action.user {
        background-color: #c714c7;
    }
    .action.delete {
        background-color: #dc3545;
    }
    .action.delete:hover {
        background-color: #c82333;
    }
    .action.edit {
        background-color: #19b74b;
    }
    .action.edit:hover {
        background-color: #157a3b;
    }
`;

export default ManageUsers;
