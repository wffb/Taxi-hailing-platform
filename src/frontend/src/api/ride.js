import api from "./client";

// export async function fetchAllRides() {
//     const res = await api.get("/ride/getAllRides");
//     const payload = res?.data;
//     return payload?.data || [];
// }
export async function fetchAllRides() {
    const res = await api.get("/ride/getAllRides"); 
    if (res?.code !== 200) {
        throw new Error(res?.message || "Failed to fetch rides");
    }
    return res?.data || [];   
}


export async function acceptRide({ rideId, driverId }) {
    const body = {
        ride_id: Number(rideId),
        driver_id: Number(driverId),
    };

    const res = await api.post("/ride/accept", body);
    return res;
}

function normaliseStartTime(startTime) {
    if (!startTime) return undefined;
    try {
        const iso = new Date(startTime).toISOString();
        if (Number.isNaN(new Date(iso).getTime())) return undefined;
        return { iso, raw: startTime };
    } catch (err) {
        console.warn("[frontend] unable to normalise start time", err);
        return undefined;
    }
}

// export async function requestRide({
//                                       riderId,
//                                       pickupPostcode,
//                                       destinationPostcode,
//                                       startTime,
//                                       pickup,
//                                       destination,
//                                   }) {
//     const pickupValue = (pickupPostcode ?? pickup ?? "").toString().trim();
//     const destinationValue = (destinationPostcode ?? destination ?? "").toString().trim();

//     const payload = {
//         rider_id: Number(riderId),
//         pickup_location: pickupValue,
//         destination: destinationValue,
//         pickup_postcode: pickupValue,
//         destination_postcode: destinationValue,
//     };

//     const start = normaliseStartTime(startTime);
//     if (start) {
//         payload.start_time = start.iso;
//         payload.startTime = start.iso;
//         payload.requested_start_time = start.iso;
//         payload.start_time_local = start.raw;
//     }

//     const res = await api.post("/ride/requestRide", payload);
//     return res?.data;
// }

export async function requestRide({ riderId, pickup, destination }) {
    const payload = {
        rider_id: Number(riderId),
        pickup_location: (pickup ?? "").toString().trim(),
        destination: (destination ?? "").toString().trim(),
    };

    const res = await api.post("/ride/requestRide", payload);
    console.log("requestRide response:", res);
    return res;   
}




