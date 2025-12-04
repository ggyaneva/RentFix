package app.feign.dto;


import app.model.enums.MaintenanceStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MaintenanceView {

    private UUID id;
    private String description;
    private MaintenanceStatus status;
    private LocalDateTime createdAt;

    private String propertyTitle;
    private String propertyCity;

    private String tenantName;
    private String ownerName;
    private UUID propertyId;

}

