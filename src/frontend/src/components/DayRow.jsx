/* eslint-disable react/prop-types */

const DAY_NAMES = { 1: "Mon", 2: "Tue", 3: "Wed", 4: "Thu", 5: "Fri", 6: "Sat", 7: "Sun" };

// Controlled component that renders multiple time segments for a day
export default function DayRow({ value, onChange }) {
    const { dayOfWeek, segments } = value;

    const updateSegment = (idx, next) => {
        const nextSegs = segments.map((s, i) => (i === idx ? next : s));
        onChange({ ...value, segments: nextSegs });
    };

    const addSegment = () => {
        const nextSegs = [...segments, { startTime: "09:00", endTime: "10:00" }];
        onChange({ ...value, segments: nextSegs });
    };

    const removeSegment = (idx) => {
        const nextSegs = segments.filter((_, i) => i !== idx);
        onChange({ ...value, segments: nextSegs });
    };

    return (
        <>
            {segments.map((seg, idx) => (
                <tr key={idx}>
                    <td>{idx === 0 ? DAY_NAMES[dayOfWeek] : ""}</td>
                    <td>
                        <input
                            type="time"
                            value={seg.startTime}
                            onChange={(e) => updateSegment(idx, { ...seg, startTime: e.target.value })}
                        />
                    </td>
                    <td>
                        <input
                            type="time"
                            value={seg.endTime}
                            onChange={(e) => updateSegment(idx, { ...seg, endTime: e.target.value })}
                        />
                    </td>
                    <td>
                        <button type="button" onClick={() => removeSegment(idx)}>Remove</button>
                    </td>
                </tr>
            ))}
            <tr>
                <td>{segments.length === 0 ? DAY_NAMES[dayOfWeek] : ""}</td>
                <td colSpan={2}>
                    <button type="button" onClick={addSegment}>Add</button>
                </td>
                <td></td>
            </tr>
        </>
    );
}
