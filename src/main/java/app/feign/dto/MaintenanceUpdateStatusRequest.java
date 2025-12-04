package app.feign.dto;

import app.model.enums.MaintenanceStatus;
import lombok.Data;

@Data
public class MaintenanceUpdateStatusRequest {
    private MaintenanceStatus status;

}
