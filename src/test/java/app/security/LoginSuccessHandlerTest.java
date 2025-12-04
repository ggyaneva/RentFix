package app.security;

import app.model.enums.Role;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoginSuccessHandlerTest {

    private final LoginSuccessHandler handler = new LoginSuccessHandler();

    @Test
    void onAuthenticationSuccess_setsSessionAndRedirectsToDashboard() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        UUID userId = UUID.randomUUID();
        UserData principal = new UserData(
                userId,
                "testUser",
                "pass",
                Role.TENANT,
                true
        );

        Authentication auth = new TestingAuthenticationToken(principal, null);

        handler.onAuthenticationSuccess(request, response, auth);

        HttpSession session = request.getSession(false);
        assertNotNull(session);
        assertEquals(userId, session.getAttribute("userId"));
        assertEquals("TENANT", session.getAttribute("role"));
        assertEquals("/dashboard", response.getRedirectedUrl());
    }
}