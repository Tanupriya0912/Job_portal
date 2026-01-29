import axios from "axios";
import React, { useEffect } from "react";
import styled from "styled-components";
import LoadingComTwo from "../shared/LoadingComTwo";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { updateHandler, buildApiUrl } from "../../utils/FetchHandlers";
import Swal from "sweetalert2";

const Recruiter = () => {
    const {
    isLoading,
        isError,
        data: jobs,
        error,
        refetch,
    } = useQuery({
        queryKey: ["rec-jobs"],
        queryFn: async () => {
            // Backend exposes recruiter applications at /api/v1/application/recruiter-applications
            const response = await axios.get(
                buildApiUrl(`/api/v1/application/recruiter-applications`),
                {
                    withCredentials: true,
                }
            );
            return response?.data?.result;
        },
        // Refetch every 5 seconds to keep data fresh
        refetchInterval: 5000,
    });

    const queryClient = useQueryClient();

    // If backend didn't include jobPosition/jobCompany, fetch job details per application
    useEffect(() => {
        if (!jobs || jobs.length === 0) return;

        const missing = jobs.filter((j) => !j.jobPosition || !j.jobCompany);
        if (missing.length === 0) return;

        const fetchDetails = async () => {
            try {
                const results = await Promise.all(
                    missing.map(async (app) => {
                        const jobId = app.jobId || app.job?.id || app.id;
                        if (!jobId) return null;
                        try {
                            const res = await axios.get(buildApiUrl(`/api/v1/jobs/${jobId}`), { withCredentials: true });
                            const payload = res?.data?.result || res?.data || null;
                            if (!payload) return null;
                            return {
                                applicationId: app.id,
                                jobPosition: payload.position || payload.jobPosition || payload.positionName,
                                jobCompany: payload.company || payload.jobCompany,
                            };
                        } catch (e) {
                            return null;
                        }
                    })
                );

                const valid = results.filter(Boolean);
                if (valid.length === 0) return;

                queryClient.setQueryData(["rec-jobs"], (old) => {
                    if (!Array.isArray(old)) return old;
                    return old.map((item) => {
                        const found = valid.find((r) => r.applicationId === item.id);
                        if (found) {
                            return { ...item, jobPosition: item.jobPosition || found.jobPosition, jobCompany: item.jobCompany || found.jobCompany };
                        }
                        return item;
                    });
                });
            } catch (e) {
                // ignore
            }
        };

        fetchDetails();
    }, [jobs, queryClient]);

    // Refetch when page becomes visible
    useEffect(() => {
        const handleVisibilityChange = () => {
            if (!document.hidden) {
                refetch();
            }
        };
        
        document.addEventListener("visibilitychange", handleVisibilityChange);
        return () => {
            document.removeEventListener("visibilitychange", handleVisibilityChange);
        };
    }, [refetch]);

    const updateJobStatusMutation = useMutation({
        mutationFn: updateHandler,
        onSuccess: (data, variable, context) => {
            refetch();
            Swal.fire({
                icon: "success",
                title: "Status Updated",
                text: data?.message || "Status updated successfully",
            });
        },
        onError: (error) => {
            console.error("Update status failed:", error);
            const msg = error?.response?.data?.message || JSON.stringify(error?.response?.data) || error?.message || "Failed to update status";
            Swal.fire({
                icon: "error",
                title: "Oops...",
                text: msg,
            });
        },
    });

    const handleAcceptStatus = (id, recruiterId) => {
        const newStatus = { recruiterId, status: "accepted" };
        updateJobStatusMutation.mutate({
            body: newStatus,
            url: buildApiUrl(`/api/v1/application/${id}`),
        });
    };

    const handleRejectStatus = (id, recruiterId) => {
        const newStatus = { recruiterId, status: "rejected" };
        updateJobStatusMutation.mutate({
            body: newStatus,
            url: buildApiUrl(`/api/v1/application/${id}`),
        });
    };

    const handleResumeView = async (applicationId) => {
        if (!applicationId) {
            Swal.fire({
                icon: "warning",
                title: "No Resume",
                text: "Applicant has not uploaded a resume yet",
            });
            return;
        }

        try {
            // backend exposes resume download at /{id}/download-resume
            const response = await axios.get(
                buildApiUrl(`/api/v1/application/${applicationId}/download-resume`),
                { 
                    withCredentials: true,
                    responseType: 'blob'
                }
            );

            // Create blob URL and download
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `resume.pdf`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        } catch (error) {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: error?.response?.data?.message || "Could not download resume",
            });
        }
    };
    if (isLoading) {
        return <LoadingComTwo />;
    }

    if (isError) {
        return (
            <h2 className="mt-8 text-2xl font-semibold text-center text-red-600">
                -- {error?.response?.data} --
            </h2>
        );
    }

    if (jobs) {
        // console.log(jobs);
    }

    if (!jobs || jobs.length === 0) {
        return <h2 className="">No Application found</h2>;
    }

    return (
        <Wrapper>
            <div className="content-row">
                <table className="table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Job Position</th>
                            <th>Company</th>
                            <th>Status</th>
                            <th>actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {jobs?.map((job, index) => {
                            let i =
                                index + 1 < 10 ? `0${index + 1}` : index + 1;
                            return (
                                <tr key={job?.id}>
                                    <td>{i}</td>
                                    <td>{job?.jobPosition || job?.position}</td>
                                    <td>{job?.jobCompany || job?.company}</td>
                                    <td>{job?.status}</td>
                                    <td className="action-row">
                                        <button
                                            className="action resume"
                                            onClick={() =>
                                                handleResumeView(job.id)
                                            }
                                        >
                                            resume
                                        </button>

                                        {job?.status === "pending" && (
                                            <>
                                                {" "}
                                                <button
                                                    className="action accept"
                                                    onClick={() =>
                                                        handleAcceptStatus(
                                                            job.id,
                                                            job?.recruiterId
                                                        )
                                                    }
                                                >
                                                    accept
                                                </button>
                                                <button
                                                    className="action reject"
                                                    onClick={() =>
                                                        handleRejectStatus(
                                                            job.id,
                                                            job?.recruiterId
                                                        )
                                                    }
                                                >
                                                    Reject
                                                </button>
                                            </>
                                        )}

                                        {job?.status === "accepted" && (
                                            <button
                                                className="action reject"
                                                onClick={() =>
                                                    handleRejectStatus(
                                                        job.id,
                                                        job?.recruiterId
                                                    )
                                                }
                                            >
                                                Reject
                                            </button>
                                        )}

                                        {job?.status === "rejected" && (
                                            <button
                                                className="action accept"
                                                onClick={() =>
                                                    handleAcceptStatus(
                                                        job.id,
                                                        job?.recruiterId
                                                    )
                                                }
                                            >
                                                accept
                                            </button>
                                        )}
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
        font-size: 12px;
        text-transform: capitalize;
        font-weight: 500;
        color: #fff;
        padding: 1px 6px;
        border-radius: 4px;
    }
    .action.accept {
        background-color: #168e24;
    }
    .action.reject {
        background-color: #f1322f;
    }
    .action.resume {
        background-color: #ef9712;
    }
`;

export default Recruiter;
