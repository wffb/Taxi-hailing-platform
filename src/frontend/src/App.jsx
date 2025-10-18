// import React from "react";
// import ScheduleForm from "./components/ScheduleForm";

// export default function App() {
//   return (
//     <div className="container">
//       <header>
//         <h1></h1>
//       </header>
//       <ScheduleForm />
//     </div>
//   );
// }
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./components/Login";
import Register from "./components/Register";
import DriverDashboard from "./components/DriverDashboard";
import DriverSchedulePage from "./components/DriverSchedulePage";
import DriverRides from "./components/DriverRides";
import RiderDashboard from "./components/RiderDashboard";

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Navigate to="/login" replace />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/driver" element={<DriverDashboard />} />
                <Route path="/driver/schedule" element={<DriverSchedulePage />} />
                <Route path="/driver/rides" element={<DriverRides />} />
                <Route path="/rider" element={<RiderDashboard />} />
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
}
