export function isDriverAvailableNow(schedules) {
    if (!Array.isArray(schedules) || schedules.length !== 7) return true;
    const hasAny = schedules.some((s) => s && /1/.test(s));
    if (!hasAny) return true;
    const now = new Date();
    const dayIndex = (now.getDay() + 6) % 7; // Monday=0
    const str = schedules[dayIndex] || "";
    const slot = now.getHours() * 2 + (now.getMinutes() >= 30 ? 1 : 0);
    return str.charAt(slot) === "1";
}
