package com.smartparking.reservationservice.client;

import com.smartparking.reservationservice.exception.SpotUnavailableException;
import com.smartparking.reservationservice.exception.UpstreamServiceException;
import com.smartparking.reservationservice.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * Calls parking-service's claim/release endpoints using a self-minted
 * internal token (JwtService.createInternalServiceToken) instead of the
 * driver's own token -- required for the expiry scheduler, which has no
 * driver request in scope at all, and used uniformly for the create/cancel
 * paths too. See the reservation-flow design notes.
 */
// IllegalStateException ("No instances available for PARKING-SERVICE") is
// what spring-cloud-loadbalancer throws when Eureka hasn't propagated a
// live instance yet (e.g. right after parking-service restarts) -- it's
// a plain IllegalStateException, not a RestClientException, so it needs
// its own catch or it escapes as an unhandled 500 instead of a clean 502.
@Component
public class ParkingServiceClient {

    private final RestClient restClient;
    private final JwtService jwtService;

    public ParkingServiceClient(RestClient.Builder loadBalancedRestClientBuilder, JwtService jwtService) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("lb://PARKING-SERVICE").build();
        this.jwtService = jwtService;
    }

    public void claimSpot(UUID spotId, UUID onBehalfOfUserId) {
        try {
            restClient.post()
                    .uri("/spots/{spotId}/claim", spotId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + internalToken(onBehalfOfUserId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            throw new SpotUnavailableException();
        } catch (RestClientException | IllegalStateException e) {
            throw new UpstreamServiceException("Could not reach parking-service to claim the spot");
        }
    }

    public void releaseSpot(UUID spotId, UUID onBehalfOfUserId) {
        try {
            restClient.post()
                    .uri("/spots/{spotId}/release", spotId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + internalToken(onBehalfOfUserId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException | IllegalStateException e) {
            throw new UpstreamServiceException("Could not reach parking-service to release the spot");
        }
    }

    private String internalToken(UUID onBehalfOfUserId) {
        return jwtService.createInternalServiceToken(onBehalfOfUserId);
    }
}
