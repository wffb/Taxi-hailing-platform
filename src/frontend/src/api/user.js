import api from "./client";


// export async function login({ username, password }) {
//   const res = await api.post("/test", { username, password });
//   const payload = res?.data || {};
//   const token = payload?.data;
//   if (token) localStorage.setItem("token", token);
//   return payload;
// }

export async function login({ username, password }) {
  const res = await api.post("/user/login", { username, password });
  const token = res?.data?.token;
  if (token) localStorage.setItem("token", token);
  return res;   // { code, message, data }
}



export async function register({ username, password,  email, role }) {
  const body = { username, password, email, role };
  const res = await api.post("/user/register", body);
  // return res?.data || {};
  return res;
}

export async function logout() {
  const res = await api.get("/user/logout");
  localStorage.removeItem("token");
  return res?.data || {};
}
