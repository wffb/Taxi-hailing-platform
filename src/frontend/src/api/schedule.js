import api from "./client";


export async function fetchAllDrivers() {
  const res = await api.get("/driver/getDrivers");
  return res.data;
}


  

function coerceScheduleArray(entry) {
    if (!entry) return null;
    if (Array.isArray(entry.schedule)) return entry.schedule;
    if (Array.isArray(entry.schedules)) return entry.schedules;
  
    const keys = [
      "schedule_mon","schedule_tue","schedule_wed",
      "schedule_thu","schedule_fri","schedule_sat","schedule_sun",
    ];
    if (keys.every(k => typeof entry[k] === "string")) {
      return keys.map(k => entry[k]);
    }
    if (typeof entry.schedule === "string") {
      try {
        const parsed = JSON.parse(entry.schedule);
        if (Array.isArray(parsed)) return parsed;
      } catch(_) {}
    }
    return null;
  }
  
function normalize48(s) {
    const ZERO48 = "0".repeat(48);
    if (typeof s !== "string") return ZERO48;
    if (s.length === 48) return s;
    if (s.length > 48) return s.slice(0, 48);
    // If the length is less than 48, make up 0 on the right side.
    return (s + ZERO48).slice(0, 48);
  }
  
function normalize7Days(arr) {
    const ZERO48 = "0".repeat(48);
    let a = Array.isArray(arr) ? arr.slice(0, 7) : [];
    // If there are only 5 days (Mon-Fri), make up Saturday and Sunday. 
    if (a.length === 5) a = [...a, ZERO48, ZERO48];
    // Less than 7 days 0
    while (a.length < 7) a.push(ZERO48);
    // Truncated for more than 7 days
    a = a.slice(0, 7);
    return a.map(normalize48);
  }
  
export async function fetchDriverSchedules(driverId) {
    if (!driverId) throw new Error("Driver ID is required");
  
    const res = await api.get("/schedules/getAll");
    let raw = res?.data?.data ?? res?.data; // Compatible with different backends envelope
  
    // Case 1: data is a string, wrapped in JSON
    if (typeof raw === "string") {
      try {
        raw = JSON.parse(raw);
      } catch (e) {
        
        try {
          raw = JSON.parse(JSON.parse(raw));
        } catch (_) {}
      }
    }
  
    // Scenario 2: Straightforward 7/5 day string array
    if (Array.isArray(raw) && raw.every(x => typeof x === "string")) {
      return normalize7Days(raw);
    }
  
    // Case 3: is an array of objects (may contain driverId)
    if (Array.isArray(raw) && raw.length && typeof raw[0] === "object") {
      const target = raw.find(it => {
        const id = it?.id ?? it?.driver_id ?? it?.driverId;
        return id !== undefined && String(id) === String(driverId);
      }) ?? raw[0]; // If you can't find it, take the first one. At least it doesn't give you an error.
  
      const arr = coerceScheduleArray(target);
      if (arr) return normalize7Days(arr);
    }
  
    // Case 4: Single object with schedule_* or schedule
    if (raw && typeof raw === "object") {
      const arr = coerceScheduleArray(raw);
      if (arr) return normalize7Days(arr);
}
  
    // Tout: return empty table (no more throw, avoid page red error)
    console.warn("fetchDriverSchedules: unexpected shape ->", raw);
    return normalize7Days([]);
  }


  


export async function updateDriverSchedules(driverId, schedules) {
    if (!driverId) throw new Error("Driver ID is required");
    if (!Array.isArray(schedules) || schedules.length !== 7) {
      throw new Error("Schedule payload must contain seven day strings.");
    }
  
    const payload = {
        // id: Number(driverId),
      schedules: schedules
    };
  
    const res = await api.post("/schedules/update", payload);
    return res.data;
  }
  
