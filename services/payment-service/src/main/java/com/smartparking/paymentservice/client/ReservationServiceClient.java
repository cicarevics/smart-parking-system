package com.smartparking.paymentservice.client;

import com.smartparking.paymentservice.exception.ReservationNotFoundException;
import com.smartparking.paymentservice.exception.UpstreamServiceException;
import com.smartparking.paymentservice.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * Two different auth strategies on purpose, not the same one everywhere.
 * fetch() and cancel() forward the driver's own token: every call here
 * happens inside a driver-initiated request, so that token is legitimately
 * in scope the whole time, and reservation-service's existing
 * findByIdAndUserId ownership check already enforces who's allowed to see
 * or cancel it -- no new mechanism needed. confirm() and extend() are the
 * exceptions: both mint their own internal token, because a driver must
 * never be able to confirm or extend their own reservation without
 * actually paying for it. See the Charge and Confirm design notes.
 */
@Component
public class ReservationServiceClient {

    private final RestClient restClient;
    private final JwtService jwtService;

    public ReservationServiceClient(RestClient.Builder loadBalancedRestClientBuilder, JwtService jwtService) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("lb://RESERVATION-SERVICE").build();
        this.jwtService = jwtService;
    }

    public ReservationView fetch(UUID reservationId, String authorizationHeader) {
        try {
            return restClient.get()
                    .uri("/{id}", reservationId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(ReservationView.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ReservationNotFoundException();
        } catch (RestClientException | IllegalStateException e) {
            throw new UpstreamServiceException("Could not reach reservation-service to look up the reservation");
        }
    }

    public void confirm(UUID reservationId, UUID driverId) {
        try {
            restClient.post()
                    .uri("/{id}/confirm", reservationId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.createInternalServiceToken(driverId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException | IllegalStateException e) {
            throw new UpstreamServiceException("Could not reach reservation-service to confirm the reservation");
        }
    }

    public void extend(UUID reservationId, UUID driverId, int additionalMinutes) {
        try {
            restClient.post()
                    .uri("/{id}/extend", reservationId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.createInternalServiceToken(driverId))
                    .body(new ExtendRequestBody(additionalMinutes))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException | IllegalStateException e) {
            throw new UpstreamServiceException("Could not reach reservation-service to extend the reservation");
        }
    }

    public void cancel(UUID reservationId, String authorizationHeader) {
        try {
            restClient.delete()
                    .uri("/{id}", reservationId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException | IllegalStateException e) {
            throw new UpstreamServiceException("Could not reach reservation-service to cancel the reservation");
        }
    }
}
