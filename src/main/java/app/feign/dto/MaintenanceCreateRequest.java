package app.feign.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class MaintenanceCreateRequest {

    private UUID propertyId;
    private UUID tenantId;
    private UUID ownerId;
    private String description;
}