import React, { useState } from "react";
import { register, login } from "../api/user";
import { z } from "zod";

const schema = z.object({
  username: z.string().min(1, "Please enter a name"),
  email: z.string().email("Email format is incorrect"),
  password: z.string().min(4, "Password must be at least 4 characters"),
  role: z.enum(["Rider", "Driver"], { required_error: "Please select a role" }),
});

export default function Register() {
  const [form, setForm] = useState({ username: "", email: "", password: "", role: "Rider" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    const parsed = schema.safeParse(form);
    if (!parsed.success) {
      setError(parsed.error.errors[0]?.message || "Form validation failed");
      return;
    }

    setLoading(true);
    try {
      // backend currently only requires username+password, here we send email as username
      const res = await register({
        username: form.username,
        password: form.password,
        // name: form.name,
        email: form.email,
        role: form.role,
      });
      if (res?.code !== 200) throw new Error(res?.message || "Registration failed");

      setSuccess("Registration successful");
      // const loginRes = await login({ username: form.username, password: form.password });
      // if (loginRes?.code !== 200) throw new Error("Registration successful, but login failed");
      localStorage.setItem("role", form.role);
      // const target = form.role === "Driver" ? "/driver" : "/rider";
      // window.location.replace(target);
      window.location.replace("/login");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Register</h2>
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          Username
          <input
            type="text"
            name="username"
            value={form.username}
            onChange={handleChange}
            placeholder="Enter username"
            required
          />
        </label>

        <label>
          Email
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="you@example.com"
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
            placeholder="At least 4 characters"
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
          {loading ? "Submitting..." : "Register"}
        </button>

        {error && <p className="error">{error}</p>}
        {success && <p className="success">{success}</p>}
      </form>

      <p className="muted">
        Have an account? <a href="/login">Login</a>
      </p>
    </div>
  );
}
