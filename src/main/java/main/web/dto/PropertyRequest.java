package main.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class PropertyRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String city;

    @NotBlank
    private String address;

    @NotBlank
    private int bedrooms;

    @NotBlank
    private int bathrooms;

    @NotBlank
    private BigDecimal areaSqm;

    @NotBlank
    private BigDecimal monthlyRent;

    private MultipartFile[] images;
}


