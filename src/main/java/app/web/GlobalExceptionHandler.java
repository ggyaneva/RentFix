package app.web;

import app.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException exception,
                                               HttpServletRequest request) {

        log.warn("Resource not found: {} at {}", exception.getMessage(), request.getRequestURI());

        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.addObject("message", exception.getMessage());
        modelAndView.addObject("path", request.getRequestURI());
        return modelAndView;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ModelAndView handleBadRequest(RuntimeException exception,
                                         HttpServletRequest request) {

        log.error("Bad request at {}: {}", request.getRequestURI(), exception.getMessage());

        ModelAndView modelAndView = new ModelAndView("error/general");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        modelAndView.addObject("message", exception.getMessage());
        modelAndView.addObject("path", request.getRequestURI());
        return modelAndView;
    }


}
