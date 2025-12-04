package app.web;

import app.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returns404View() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/some/path");

        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");

        ModelAndView mav = handler.handleResourceNotFound(ex, request);

        assertEquals("error/404", mav.getViewName());
        assertEquals(HttpStatus.NOT_FOUND, mav.getStatus());
        assertEquals("Not found", mav.getModel().get("message"));
        assertEquals("/some/path", mav.getModel().get("path"));
    }

    @Test
    void handleBadRequest_returnsGeneralErrorView() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/bad");

        IllegalArgumentException ex = new IllegalArgumentException("Bad request");

        ModelAndView mav = handler.handleBadRequest(ex, request);

        assertEquals("error/general", mav.getViewName());
        assertEquals(HttpStatus.BAD_REQUEST, mav.getStatus());
        assertEquals("Bad request", mav.getModel().get("message"));
        assertEquals("/bad", mav.getModel().get("path"));
    }
}