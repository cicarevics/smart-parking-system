package com.smartparking.paymentservice.client;

// Wire format for reservation-service's POST /{id}/extend. A plain
// Map<String,Object> would NOT work here -- Jackson's SNAKE_CASE naming
// strategy (configured in both services' application.yml) only rewrites
// POJO property names via bean introspection, not raw Map keys, so a Map
// would serialize as "additionalMinutes" instead of the
// "additional_minutes" reservation-service's ExtendRequest expects.
public class ExtendRequestBody {

    private final int additionalMinutes;

    public ExtendRequestBody(int additionalMinutes) {
        this.additionalMinutes = additionalMinutes;
    }

    public int getAdditionalMinutes() {
        return additionalMinutes;
    }
}
