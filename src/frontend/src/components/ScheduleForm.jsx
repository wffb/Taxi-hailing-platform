import { useEffect, useState } from "react";
import DayRow from "./DayRow";
import { WeekSchema } from "../utils/validation";
import { fetchDriverSchedules, updateDriverSchedules } from "../api/schedule";
import { scheduleStringsToDays, daysToScheduleStrings } from "../utils/scheduleConverter";
import api from "../api/client";

const defaultDay = (dow) => ({ dayOfWeek: dow, segments: [] });

export default function ScheduleForm({ driverId: propDriverId, initialSchedule }) {
    // const driverId = localStorage.getItem("driverId") || "";
    // const [days, setDays] = useState(Array.from({ length: 7 }, (_, i) => defaultDay(i + 1)));
    const driverId = propDriverId || localStorage.getItem("driverId") || "";
    const [days, setDays] = useState(
        initialSchedule ? scheduleStringsToDays(initialSchedule) : Array.from({ length: 7 }, (_, i) => defaultDay(i + 1))
    );

    const [loading, setLoading] = useState(false);
    const [msg, setMsg] = useState(null);
    const [err, setErr] = useState(null);

    useEffect(() => {
        if (!driverId) { setErr("Driver ID missing"); return; }
        const load = async () => {
            setLoading(true); setErr(null);
            try {
                const schedules = await fetchDriverSchedules(driverId);
                // const arr = typeof schedules === "string" ? JSON.parse(schedules) : schedules;

                const daysState = scheduleStringsToDays(schedules);
                setDays(daysState);
            } catch (e) {
                //default
                console.warn("fetchDriverSchedules failed:", e.message);
       
                setDays(Array.from({ length: 7 }, (_, i) => defaultDay(i + 1)));
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [driverId]);
    

    const updateDay = (idx, next) => {
        setDays((prev) => prev.map((d, i) => (i === idx ? next : d)));
    };

    const onSubmit = async (e) => {
        e.preventDefault();
        setMsg(null); setErr(null);

        if (!driverId) { setErr("Driver ID missing"); return; }

        const payload = { driverId: String(driverId), windows: days };
        const parsed = WeekSchema.safeParse(payload);
        if (!parsed.success) {
            setErr(parsed.error.errors?.[0]?.message || "Invalid form");
            return;
        }

        // convert the current week view into the seven 48-character strings required by the API
        const scheduleStrings = daysToScheduleStrings(days);
        if (!scheduleStrings || scheduleStrings.length !== 7 || scheduleStrings.some((s) => s.length !== 48)) {
            setErr("Internal error: schedule strings are invalid length.");
            return;
        }

        setLoading(true);
        try {
            const result = await updateDriverSchedules(driverId, scheduleStrings);
            setMsg(result?.message || "Saved successfully.");
        } catch (e) {
            setErr(e.message || "Save failed");
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={onSubmit} className="card">
            <h2>Driver Weekly Availability</h2>

            <table className="schedule">
                <thead>
                <tr><th>Day</th><th>Start</th><th>End</th><th>Actions</th></tr>
                </thead>
                <tbody>
                {days.map((d, i) => (
                    <DayRow key={d.dayOfWeek} value={d} onChange={(nx) => updateDay(i, nx)} />
                ))}
                </tbody>
            </table>

            <div className="actions">
                <button type="submit" disabled={loading}>
                    {loading ? "Saving..." : "Save"}
                </button>
            </div>

            {msg && <p className="ok">{msg}</p>}
            {err && <p className="error">{err}</p>}
        </form>
    );
}
