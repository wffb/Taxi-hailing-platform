/* eslint-disable react/prop-types */
import  { useState } from "react";
import { login } from "../api/user";
import { z } from "zod";
import { fetchDriverSchedules } from "../api/schedule";


const schema = z.object({
  username: z.string().min(3, "Please enter a username"),
  password: z.string().min(4, "Please enter a password"),
  // Since /user/login only returns token, without role, here we let the user select an identity for frontend routing
  role: z.enum(["Rider", "Driver"]).optional(),
});

export default function Login({ onLogin }) {
  const [form, setForm] = useState({ username: "", password: "", role: "Rider" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        const parsed = schema.safeParse(form);
        if (!parsed.success) {
            setError(parsed.error.errors[0]?.message || "Form validation failed");
            return;
        }
        setLoading(true);
        try {
            const res = await login({ username: form.username, password: form.password });
            if (res?.code !== 200) throw new Error(res?.message || "Login failed");

            const payload = res?.data;
            localStorage.removeItem("driverId");
            localStorage.removeItem("riderId");

            if (form.role === "Driver") {
                const did =
                    payload && typeof payload === "object"
                        ? payload.driverId ?? payload.driver_id ?? payload.id
                        : null;
                if (did !== null && did !== undefined) {
                    localStorage.setItem("driverId", String(did));
                }
            }

            

            if (form.role === "Rider") {
                const rid =
                    payload && typeof payload === "object"
                        ? payload.riderId ?? payload.rider_id ?? payload.id
                        : null;
                if (rid !== null && rid !== undefined) {
                    localStorage.setItem("riderId", String(rid));
                }
            }

            if (form.role) localStorage.setItem("role", form.role);
            if (onLogin) onLogin(form.role || "Rider");
            const target = form.role === "Driver" ? "/driver" : "/rider";
            window.location.replace(target);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <h2>Login</h2>
            <form className="auth-form" onSubmit={handleSubmit}>
                <label>
                    Username
                    <input
                        type="text"
                        name="username"
                        value={form.username}
                        onChange={handleChange}
                        placeholder="Enter username"
                        autoComplete="username"
                        required
                    />
                </label>

                <label>
                    Password
                    <input
                        type="password"
                        name="password"
                        value={form.password}
                        onChange={handleChange}
                        placeholder="Enter password"
                        autoComplete="current-password"
                        required
                    />
                </label>

                <label>
                    Role
                    <select name="role" value={form.role} onChange={handleChange}>
                        <option value="Rider">Rider</option>
                        <option value="Driver">Driver</option>
                    </select>
                </label>

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>

                {error && <p className="error">{error}</p>}
            </form>

            <p className="muted">
                Have no account? <a href="/register">Register</a>
            </p>
        </div>
    );
}
