const SLOTS_PER_DAY = 48; // 48 half-hour slots

function indexToTime(idx) {
    const totalMinutes = idx * 30;
    const hh = Math.floor(totalMinutes / 60);
    const mm = totalMinutes % 60;
    return String(hh).padStart(2, "0") + ":" + String(mm).padStart(2, "0");
}

function timeToIndex(timeStr) {
    const [hh, mm] = timeStr.split(":").map(Number);
    return hh * 2 + (mm >= 30 ? 1 : 0);
}

export function scheduleStringsToDays(scheduleStrings) {
    const defaultDay = (dow) => ({ dayOfWeek: dow, segments: [] });

    if (!Array.isArray(scheduleStrings) || scheduleStrings.length !== 7) {
        return Array.from({ length: 7 }, (_, i) => defaultDay(i + 1));
    }

    return scheduleStrings.map((str, idx) => {
        const dow = idx + 1;
        if (!str || typeof str !== "string" || str.length < SLOTS_PER_DAY) return defaultDay(dow);

        const arr = str.split("").map((c) => (c === "1" ? 1 : 0));
        const segments = [];
        let i = 0;
        while (i < SLOTS_PER_DAY) {
            if (arr[i] === 1) {
                const start = i;
                while (i < SLOTS_PER_DAY && arr[i] === 1) i++;
                const end = i; // exclusive
                segments.push({ startTime: indexToTime(start), endTime: indexToTime(end) });
            } else {
                i++;
            }
        }
        return { dayOfWeek: dow, segments };
    });
}

export function daysToScheduleStrings(days) {
    const makeZeros = () => Array.from({ length: SLOTS_PER_DAY }, () => "0");
    const result = [];

    for (let i = 0; i < 7; i++) {
        const d = days[i];
        const arr = makeZeros();
        if (d && Array.isArray(d.segments)) {
            d.segments.forEach((seg) => {
                const sIdx = timeToIndex(seg.startTime);
                const eIdx = timeToIndex(seg.endTime);
                for (let k = sIdx; k < eIdx && k < SLOTS_PER_DAY; k++) arr[k] = "1";
            });
        }
        result.push(arr.join(""));
    }

    return result;
}
