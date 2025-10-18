import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchDriverSchedules } from "../api/schedule";
import { fetchAllRides, acceptRide } from "../api/ride";
import { isDriverAvailableNow } from "../utils/availability";

export default function DriverRides() {
    const driverId = localStorage.getItem("driverId") || "";
    const [rides, setRides] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [available, setAvailable] = useState(true);
    const [notice, setNotice] = useState(null);
    const [acceptingId, setAcceptingId] = useState(null);

    useEffect(() => {
        const load = async () => {
            if (!driverId) {
                setError("Driver ID missing");
                setLoading(false);
                return;
            }
            setNotice(null);
            try {
                const schedules = await fetchDriverSchedules(driverId);
                const avail = isDriverAvailableNow(schedules);
                setAvailable(avail);
                if (avail) {
                    const all = await fetchAllRides();
                    const filtered = all.filter((r) => {
                        const state = (r.ride_state || r.rideState || "").toUpperCase();
                        const did = r.driver_id ?? r.driverId;
                        return state === "REQUESTED" && (did === null || did === undefined || did === 0);
                    });
                    setRides(filtered);
                } else {
                    setRides([]);
                }
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [driverId]);

    const handleAccept = async (rideId) => {
        if (!driverId || !rideId) return;
        setNotice(null);
        setAcceptingId(rideId);
        try {
            const res = await acceptRide({ rideId, driverId });
            if (res?.code !== 200) {
                throw new Error(res?.message || "Failed to accept ride");
            }
            setRides((prev) => prev.filter((r) => (r.ride_id || r.rideId) !== rideId));
            setNotice({ type: "success", message: "Ride accepted successfully." });
        } catch (err) {
            setNotice({ type: "error", message: err.message });
        } finally {
            setAcceptingId(null);
        }
    };

    if (!driverId) {
        return (
            <div className="container rides-page">
                <div className="page-header">
                    <Link to="/login" className="back-link">
                        ← Back to login
                    </Link>
                    <h1>Ride requests</h1>
                </div>
                <div className="card">
                    <p className="error">Driver ID missing. Please log in as a driver to continue.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="container rides-page">
            <div className="page-header">
                <Link to="/driver" className="back-link">
                    ← Back to dashboard
                </Link>
                <h1>Ride requests</h1>
                <p className="page-subtitle">
                    Review riders waiting for assignment when you are online during one of your available
                    time windows.
                </p>
            </div>

            <div className="card ride-card">
                {notice?.message && (
                    <p className={notice.type === "success" ? "ok" : "error"}>{notice.message}</p>
                )}
                {!available && !loading && (
                    <p className="muted">
                        You are currently outside your available time range. Update your
                        <Link to="/driver/schedule"> schedule</Link> to start receiving new requests.
                    </p>
                )}
                {loading && <p>Loading...</p>}
                {error && <p className="error">{error}</p>}
                {!loading && available && !error && (
                    <>
                        {rides.length === 0 ? (
                            <p>No ride requests available.</p>
                        ) : (
                            <ul className="ride-list">
                                {rides.map((r) => {
                                    const rideId = r.ride_id || r.rideId;
                                    // const pickup = r.pickup_location || r.pickupLocation;
                                    // const destination = r.destination || r.destinationPostcode;

                                    const pickup =
                                        r.pickup_postcode ||
                                        r.pickupPostcode ||
                                        r.pickup_location ||
                                        r.pickupLocation;
                                    const destination =
                                        r.destination_postcode ||
                                        r.destinationPostcode ||
                                        r.destination;
                                    const fare = r.estimate_fare ?? r.estimateFare;
                                    const startTimeRaw =
                                        r.start_time ||
                                        r.startTime ||
                                        r.requested_start_time ||
                                        r.start_time_local;
                                    const startTime = (() => {
                                        if (!startTimeRaw) return null;
                                        const date = new Date(startTimeRaw);
                                        if (Number.isNaN(date.getTime())) return startTimeRaw;
                                        return date.toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });
                                    })();
                                    return (
                                        <li className="ride-item" key={rideId}>
                                            <div className="ride-title">
                                                {pickup} → {destination}
                                            </div>
                                            <div className="ride-meta">
                                                Requested by {r.rider_name || r.riderName || "Unknown rider"}
                                            </div>
                                            {startTime && <div className="ride-meta">Pickup time: {startTime}</div>}
                                            {fare !== undefined && fare !== null && (
                                                <div className="ride-meta">Estimated fare: ${Number(fare).toFixed(2)}</div>
                                            )}
                                            <div className="ride-actions">
                                                <button
                                                    type="button"
                                                    className="pill-button primary"
                                                    onClick={() => handleAccept(rideId)}
                                                    disabled={acceptingId === rideId}
                                                >
                                                    {acceptingId === rideId ? "Accepting..." : "Accept ride"}
                                                </button>
                                            </div>
                                        </li>
                                    );
                                })}
                            </ul>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}
