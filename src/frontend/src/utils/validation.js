import { z } from "zod";

// Basic validation: 24-hour format "HH:mm"
const timeRegex = /^([01]\d|2[0-3]):[0-5]\d$/;

export const WeekSchema = z
    .object({
        driverId: z.string().min(1, "Driver ID is required"),
        windows: z.array(
            z.object({
                dayOfWeek: z.number().int().min(1).max(7),
                segments: z.array(
                    z.object({
                        startTime: z.string().regex(timeRegex, "Invalid time (HH:mm)"),
                        endTime: z.string().regex(timeRegex, "Invalid time (HH:mm)"),
                    })
                )
            })
        )
    })
    .refine(
        (data) => data.windows.some((w) => w.segments.length > 0),
        "Select at least one available slot"
    )
    .refine(
        (data) =>
            data.windows.every((w) =>
                w.segments.every((s) => s.startTime.localeCompare(s.endTime) < 0)
            ),
        "Start time must be earlier than end time"
    );
