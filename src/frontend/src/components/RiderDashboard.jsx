import { useMemo, useState } from "react";
import { estimateFareFromPostcodes, formatFare } from "../utils/fare";
import { requestRide } from "../api/ride";
import { logout } from "../api/user";
import { useNavigate } from "react-router-dom";


export default function RiderDashboard() {
    const riderId = localStorage.getItem("riderId") || "";
    const [form, setForm] = useState({ pickupPostcode: "", destinationPostcode: "", startTime: "" });
    const [quote, setQuote] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [lastRideId, setLastRideId] = useState(null);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");
        setLastRideId(null);

        try {
            if (!form.startTime) {
                throw new Error("Please choose your pickup start time before requesting a ride.");
            }
            const estimate = estimateFareFromPostcodes(form.pickupPostcode, form.destinationPostcode);
            setQuote({ ...estimate, startTime: form.startTime });
        } catch (err) {
            setQuote(null);
            setError(err.message);
        }
    };

    const handleConfirm = async () => {
        if (!riderId) {
            setError("Rider ID missing. Please log in again to request a ride.");
            return;
        }
        if (!form.startTime) {
            setError("Pickup start time is required to submit the ride request.");
            return;
        }
        setLoading(true);
        setError("");
        setSuccess("");

        try {
            // const res = await requestRide({
            //     riderId,
            //     pickupPostcode: form.pickupPostcode.trim(),
            //     destinationPostcode: form.destinationPostcode.trim(),
            //     startTime: form.startTime,
            // });
            const res = await requestRide({
                riderId,
                pickup: form.pickupPostcode.trim(),
                destination: form.destinationPostcode.trim(),
            });
            
            if (res?.code !== 200) {
                throw new Error(res?.message || "Ride request failed");
            }
            setLastRideId(res?.data?.ride_id ?? res?.data?.rideId ?? null);
            setSuccess("Ride requested successfully. A driver will be assigned soon.");
            setForm({ pickupPostcode: "", destinationPostcode: "", startTime: "" });
            setQuote(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = () => {
        setQuote(null);
        setSuccess("");
        setError("");
    };

    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await logout();
        } catch (err) {
            console.warn("Logout failed:", err.message);
        } finally {
            localStorage.removeItem("token");
            localStorage.removeItem("riderId");
            localStorage.removeItem("driverId");
            navigate("/login");
        }
    };


    const formattedStartTime = useMemo(() => {
        if (!quote?.startTime) return null;
        const date = new Date(quote.startTime);
        if (Number.isNaN(date.getTime())) return quote.startTime;
        return date.toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });
    }, [quote?.startTime]);

    return (
        <div className="container">
            <div className="topbar">
                <button className="pill-button" onClick={handleLogout}>
                    Log out
                </button>
            </div>

            <div className="dashboard-page rider-page">
                <section className="dashboard-hero card rider-hero">
                    <span className="dashboard-kicker">Rider travel hub</span>
                    <h1>Plan your next trip with confidence</h1>
                    <p>
                        Request a ride in seconds and preview the fare before you confirm. Your pickup and
                        destination postcodes help us match you with the best driver nearby.
                    </p>

                    <div className="dashboard-meta">
                        {riderId ? (
                            <span className="dashboard-badge">Rider ID&nbsp;#{riderId}</span>
                        ) : (
                            <p className="muted">
                                We could not find your rider profile. <a href="/login">Log in</a> again to enable
                                ride requests.
                            </p>
                        )}
                    </div>
                </section>

                <section className="card ride-request-card">
                    <h2>Request a ride</h2>
                    <p className="page-subtitle">
                        Enter your pickup and destination postcodes to receive a fare estimate using the
                        city&apos;s fixed-zone model.
                    </p>

                    {quote ? (
                        <div className="quote-summary">
                            <h3>Confirm your ride</h3>
                            <div className="quote-grid">
                                <div>
                                    <span className="label">Pickup time</span>
                                    <span className="value">{formattedStartTime || form.startTime}</span>
                                    <span className="hint">Local date &amp; time</span>
                                </div>
                                <div>
                                    <span className="label">Pickup</span>
                                    <span className="value">{form.pickupPostcode}</span>
                                    <span className="hint">{quote.pickup?.zoneLabel}</span>
                                </div>
                                <div>
                                    <span className="label">Destination</span>
                                    <span className="value">{form.destinationPostcode}</span>
                                    <span className="hint">{quote.destination?.zoneLabel}</span>
                                </div>
                                <div>
                                    <span className="label">Fare type</span>
                                    <span className="value">{quote.ruleLabel}</span>
                                    <span className="hint">{quote.description}</span>
                                </div>
                                <div>
                                    <span className="label">Estimated fare</span>
                                    <span className="value highlight">{formatFare(quote.fare)}</span>
                                    <span className="hint">
                    {quote.ruleLabel
                        ? `Based on the ${quote.ruleLabel.toLowerCase()}`
                        : "Calculated via fixed-zone pricing"}
                  </span>
                                </div>
                            </div>

                            <div className="quote-actions">
                                <button type="button" className="pill-button" onClick={handleEdit} disabled={loading}>
                                    Edit details
                                </button>
                                <button
                                    type="button"
                                    className="pill-button primary"
                                    onClick={handleConfirm}
                                    disabled={loading}
                                >
                                    {loading ? "Submitting..." : "Confirm ride"}
                                </button>
                            </div>
                        </div>
                    ) : (
                        <form className="ride-request-form" onSubmit={handleSubmit}>
                            <label>
                                Pickup time
                                <input
                                    type="datetime-local"
                                    name="startTime"
                                    value={form.startTime}
                                    onChange={handleChange}
                                    disabled={loading || !riderId}
                                    required
                                />
                            </label>

                            <label>
                                Pickup postcode
                                <input
                                    type="text"
                                    name="pickupPostcode"
                                    inputMode="numeric"
                                    maxLength={4}
                                    value={form.pickupPostcode}
                                    onChange={handleChange}
                                    placeholder="e.g. 2040"
                                    disabled={loading || !riderId}
                                    required
                                />
                            </label>

                            <label>
                                Destination postcode
                                <input
                                    type="text"
                                    name="destinationPostcode"
                                    inputMode="numeric"
                                    maxLength={4}
                                    value={form.destinationPostcode}
                                    onChange={handleChange}
                                    placeholder="e.g. 3000"
                                    disabled={loading || !riderId}
                                    required
                                />
                            </label>

                            <button type="submit" className="pill-button primary" disabled={loading || !riderId}>
                                Get fare estimate
                            </button>
                        </form>
                    )}

                    {success && <p className="ok">{success}</p>}
                    {lastRideId && <p className="muted">Ride reference: #{lastRideId}</p>}
                    {error && <p className="error">{error}</p>}
                </section>
            </div>
        </div>
    );
}
