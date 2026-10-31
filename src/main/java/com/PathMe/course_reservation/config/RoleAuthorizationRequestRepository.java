
package com.PathMe.course_reservation.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class RoleAuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String ROLE_PARAMETER = "role";

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest>
            delegate =
            new org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(
            HttpServletRequest request) {
        return delegate.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (authorizationRequest != null) {
            String role = request.getParameter(ROLE_PARAMETER);

            if (role == null || role.isBlank()) {
                throw new IllegalArgumentException(
                        "Role is required to start Google signup"
                );
            }

            if (!role.equals("STUDENT") && !role.equals("PARENT")) {
                throw new IllegalArgumentException("Invalid signup role");
            }

            authorizationRequest = OAuth2AuthorizationRequest
                    .from(authorizationRequest)
                    .attributes(attributes ->
                            attributes.put(ROLE_PARAMETER, role))
                    .build();
        }

        delegate.saveAuthorizationRequest(
                authorizationRequest, request, response
        );
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response) {
        return delegate.removeAuthorizationRequest(request, response);
    }
}
