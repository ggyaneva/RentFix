package app.web.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContractRequest {

    @NotBlank
    private LocalDate startDate;
}


