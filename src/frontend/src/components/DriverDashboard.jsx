import { Link } from "react-router-dom";
import { logout } from "../api/user";
import { useNavigate } from "react-router-dom";



export default function DriverDashboard() {
    const driverId = localStorage.getItem("driverId");

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



    return (
        <div className="container">
            <div className="topbar">
                <button className="pill-button" onClick={handleLogout}>
                    Log out
                </button>
            </div>

            <div className="dashboard-page">
                <section className="dashboard-hero card">
                    <span className="dashboard-kicker">Driver control center</span>
                    <h1>Stay on top of your rides</h1>
                    <p>
                        Keep your weekly availability up to date and jump straight into new ride requests
                        when you are ready to drive.
                    </p>

                    <div className="dashboard-meta">
                        {driverId ? (
                            <span className="dashboard-badge">Driver ID&nbsp;#{driverId}</span>
                        ) : (
                            <p className="muted">
                                No driver profile detected. <Link to="/login">Log in</Link> again if you need to
                                refresh your session.
                            </p>
                        )}
                    </div>

                    <div className="dashboard-hero-actions">
                        <Link className="button-link primary" to="/driver/schedule">
                            Manage weekly schedule
                        </Link>
                        <Link className="button-link" to="/driver/rides">
                            View ride requests
                        </Link>
                    </div>
                </section>

            </div>
        </div>
    );
}
