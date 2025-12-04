package app.feign;

import app.feign.dto.MaintenanceCreateRequest;
import app.feign.dto.MaintenanceResponse;
import app.feign.dto.MaintenanceUpdateStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "maintenance-service", url = "http://localhost:8081")
public interface MaintenanceClient {

    @PostMapping("/api/maintenance")
    MaintenanceResponse create(@RequestBody MaintenanceCreateRequest request);

    @PutMapping("/api/maintenance/{id}/status")
    MaintenanceResponse updateStatus(@PathVariable UUID id,
                                     @RequestBody MaintenanceUpdateStatusRequest request);

    @GetMapping("/api/maintenance/property/{propertyId}")
    List<MaintenanceResponse> getByProperty(@PathVariable UUID propertyId);

    @GetMapping("/api/maintenance/all")
    List<MaintenanceResponse> getAll();

    @DeleteMapping("/api/maintenance/{id}")
    void delete(@PathVariable UUID id);

}

