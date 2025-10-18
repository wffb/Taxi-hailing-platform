// import { Link } from "react-router-dom";
// import ScheduleForm from "./ScheduleForm";

// export default function DriverSchedulePage() {
//     const driverId = localStorage.getItem("driverId");
    

//     return (
//         <div className="container schedule-page">
//             <div className="page-header">
//                 <Link to="/driver" className="back-link">
//                     ← Back to dashboard
//                 </Link>
//                 <h1>Weekly schedule</h1>
//                 <p className="page-subtitle">
//                     Plan when you are available to accept trips. Add multiple time slots to cover busy hours
//                     and keep your calendar in sync with riders expectations.
//                 </p>
//                 {driverId ? (
//                     <span className="dashboard-badge">Driver ID&nbsp;#{driverId}</span>
//                 ) : (
//                     <p className="error">
//                         We could not find your driver ID. Please log in again before saving your availability.
//                     </p>
//                 )}
//             </div>

//             <ScheduleForm />
//         </div>
//     );
// }


import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import ScheduleForm from "./ScheduleForm";
import { fetchDriverSchedules } from "../api/schedule";

export default function DriverSchedulePage() {
    const [driverId, setDriverId] = useState(localStorage.getItem("driverId") || null);
    const [schedule, setSchedule] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const load = async () => {
            if (!driverId) return;
            try {
                const data = await fetchDriverSchedules(driverId);
                setSchedule(data);
            } catch (err) {
                console.error("Failed to load schedule", err);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [driverId]);

    return (
        <div className="container schedule-page">
            <div className="page-header">
                <Link to="/driver" className="back-link">← Back to dashboard</Link>
                <h1>Weekly schedule</h1>
                {driverId ? (
                    <span className="dashboard-badge">Driver ID #{driverId}</span>
                ) : (
                    <p className="error">No driver ID found</p>
                )}
            </div>

    
            <ScheduleForm driverId={driverId} initialSchedule={schedule} />

            {loading && <p>Loading...</p>}
        </div>
    );
}


