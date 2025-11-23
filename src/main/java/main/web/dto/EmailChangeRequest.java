package main.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailChangeRequest {

    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    private String newEmail;
}

