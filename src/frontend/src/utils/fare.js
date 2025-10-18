const AIRPORT_POSTCODE = "3045";
const VICTORIA_PREFIX = "3";
const METRO_MIN = 3000;
const METRO_MAX = 3299;

const RULE_DETAILS = {
    AIRPORT: {
        fare: 60,
        label: "Airport Fare",
        description: "Fixed $60 fare for trips to or from Melbourne Airport (3045).",
    },
    INTERSTATE: {
        fare: 500,
        label: "Interstate Fare",
        description: "Fixed $500 fare when a trip begins or ends outside Victoria (Zone 3).",
    },
    REGIONAL: {
        fare: 220,
        label: "Regional Fare",
        description: "Fixed $220 fare for Regional Victoria trips (Zone 2).",
    },
    METRO: {
        fare: 40,
        label: "Metro Fare",
        description: "Fixed $40 fare for Metro Melbourne trips (Zone 1).",
    },
};

function normalizePostcode(value) {
    if (value === null || value === undefined) return null;
    const str = String(value).trim();
    if (!/^\d{4}$/.test(str)) return null;
    return str;
}

function classifyPostcode(value) {
    const postcode = normalizePostcode(value);
    if (!postcode) {
        throw new Error("Postcodes must be four numeric digits (e.g. 3000).");
    }

    if (postcode === AIRPORT_POSTCODE) {
        return {
            postcode,
            type: "AIRPORT",
            label: "Melbourne Airport",
            zoneLabel: "Airport (3045)",
        };
    }

    if (!postcode.startsWith(VICTORIA_PREFIX)) {
        return {
            postcode,
            type: "INTERSTATE",
            label: "Outside Victoria",
            zoneLabel: "Zone 3 – Interstate",
        };
    }

    const numeric = Number(postcode);
    if (!Number.isFinite(numeric)) {
        throw new Error("Unable to determine a pricing zone for the provided postcode.");
    }

    if (numeric >= METRO_MIN && numeric <= METRO_MAX) {
        return {
            postcode,
            type: "METRO",
            label: "Metro Melbourne",
            zoneLabel: "Zone 1 – Metro Melbourne",
        };
    }

    return {
        postcode,
        type: "REGIONAL",
        label: "Regional Victoria",
        zoneLabel: "Zone 2 – Regional Victoria",
    };
}

export function estimateFareFromPostcodes(pickup, destination) {
    const pickupInfo = classifyPostcode(pickup);
    const destinationInfo = classifyPostcode(destination);

    const categories = [pickupInfo.type, destinationInfo.type];
    let rule = "METRO";

    if (categories.includes("AIRPORT")) {
        rule = "AIRPORT";
    } else if (categories.includes("INTERSTATE")) {
        rule = "INTERSTATE";
    } else if (categories.includes("REGIONAL")) {
        rule = "REGIONAL";
    }

    const { fare, label, description } = RULE_DETAILS[rule];

    return {
        fare: Number(fare.toFixed(2)),
        rule,
        ruleLabel: label,
        description,
        pickup: pickupInfo,
        destination: destinationInfo,
    };
}

export function formatFare(amount) {
    return `$${Number(amount || 0).toFixed(2)}`;
}
