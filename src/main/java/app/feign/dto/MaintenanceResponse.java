package app.feign.dto;

import app.model.enums.MaintenanceStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MaintenanceResponse {

    private UUID id;
    private UUID propertyId;
    private UUID tenantId;
    private UUID ownerId;
    private String description;
    private MaintenanceStatus status;
    private LocalDateTime createdAt;
}