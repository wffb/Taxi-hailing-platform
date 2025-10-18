// Unified axios instance for easier baseURL changes and error handling
// import axios from "axios";

// const api = axios.create({
//   baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
//   // If adding cookie auth in the future, can enable: withCredentials: true
// });

// api.interceptors.request.use((config) => {
//   const token = localStorage.getItem("token");
//   if (token) config.headers["token"] = token;
//   return config;
// });


// // Unify error messages
// // api.interceptors.response.use(
// //   (res) => res,
// //   (err) => {
// //     const msg =
// //       err?.response?.data?.message ||
// //       err?.response?.data?.error ||
// //       err.message ||
// //       "Network error";
// //     return Promise.reject(new Error(msg));
// //   }
// // );
// api.interceptors.response.use(
//   (res) => res.data, 
//   (err) => {
//     if (err.response && err.response.data) {
//       return err.response.data; 
//     }
//     return Promise.reject(new Error(err.message || "Network error"));
//   }
// );

// export default api;


import axios from "axios";

const api = axios.create({
  
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
});

// The following APIs are not allowed to add token automatically
const AUTH_WHITELIST = ["/test", "/user/login", "/user/register"];

api.interceptors.request.use((config) => {
  const url = config.url || "";

  // If it is a login/registration, ensure that no token is added (to avoid CORS pre-flight failure)
  const isAuthApi = AUTH_WHITELIST.some((p) => url.startsWith(p));
  if (isAuthApi) {
    if (config.headers) {
      delete config.headers.token;
      delete config.headers.Authorization;
    }
  } else {
    const token = localStorage.getItem("token");
    if (token) config.headers["token"] = token;
  }

  // Uniform JSON
  if (!config.headers["Content-Type"]) {
    config.headers["Content-Type"] = "application/json";
  }
  return config;
});

// Return res.data; give a friendly prompt when network/CORS errors
api.interceptors.response.use(
  (res) => res.data,
  (err) => {
    if (!err.response) {
      return Promise.reject(new Error("Network Error")); // Pre-flight failure will go here
    }
    const data = err.response.data || {};
    return Promise.reject(new Error(data.message || "Request failed"));
  }
);

export default api;
