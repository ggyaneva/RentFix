package app.service;

import app.feign.MaintenanceClient;
import app.feign.dto.MaintenanceCreateRequest;
import app.feign.dto.MaintenanceResponse;
import app.feign.dto.MaintenanceUpdateStatusRequest;
import app.feign.dto.MaintenanceView;
import app.model.Property;
import app.model.User;
import app.model.enums.MaintenanceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaintenanceFacade {

    private final MaintenanceClient maintenanceClient;
    private final PropertyService propertyService;
    private final UserService userService;
    private final ContractService contractService;

    @Autowired
    public MaintenanceFacade(MaintenanceClient maintenanceClient, PropertyService propertyService, UserService userService, ContractService contractService) {
        this.maintenanceClient = maintenanceClient;
        this.propertyService = propertyService;
        this.userService = userService;
        this.contractService = contractService;
    }

    public void createForTenant(UUID tenantId, String description) {

        UUID propertyId = contractService.getActivePropertyId(tenantId);
        if (propertyId == null) {
            throw new IllegalStateException("You don't have an active property.");
        }

        Property property = propertyService.getById(propertyId);
        UUID ownerId = property.getOwner().getId();

        MaintenanceCreateRequest request = new MaintenanceCreateRequest();
        request.setPropertyId(propertyId);
        request.setTenantId(tenantId);
        request.setOwnerId(ownerId);
        request.setDescription(description);

        maintenanceClient.create(request);
    }

    public List<MaintenanceView> getForTenant(UUID tenantId) {

        UUID propertyId = contractService.getActivePropertyId(tenantId);
        if (propertyId == null) {
            return List.of();
        }

        List<MaintenanceResponse> responses = maintenanceClient.getByProperty(propertyId);

        Property property = propertyService.getById(propertyId);
        User tenant = userService.getById(tenantId);
        User owner = userService.getById(property.getOwner().getId());

        return responses.stream()
                .map(r -> {
                    MaintenanceView v = new MaintenanceView();
                    v.setId(r.getId());
                    v.setDescription(r.getDescription());
                    v.setStatus(r.getStatus());
                    v.setCreatedAt(r.getCreatedAt());

                    v.setPropertyId(r.getPropertyId());
                    v.setPropertyTitle(property.getTitle());
                    v.setPropertyCity(property.getCity());

                    v.setTenantName(tenant.getUsername());
                    v.setOwnerName(owner.getUsername());

                    return v;
                })
                .toList();
    }

    public List<MaintenanceView> getForOwner(UUID ownerId) {

        List<Property> properties = propertyService.getAllByOwner(ownerId);
        if (properties.isEmpty()) {
            return List.of();
        }

        Map<UUID, Property> propertyById = properties.stream()
                .collect(Collectors.toMap(Property::getId, p -> p));

        Set<UUID> ownerPropertyIds = properties.stream()
                .map(Property::getId)
                .collect(Collectors.toSet());

        List<MaintenanceResponse> all = maintenanceClient.getAll();

        List<MaintenanceResponse> filtered = all.stream()
                .filter(r -> ownerPropertyIds.contains(r.getPropertyId()))
                .toList();

        if (filtered.isEmpty()) {
            return List.of();
        }

        List<UUID> tenantIds = filtered.stream()
                .map(MaintenanceResponse::getTenantId)
                .distinct()
                .toList();

        Map<UUID, User> usersById = userService.getAllByIds(tenantIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return filtered.stream()
                .map(r -> {
                    MaintenanceView maintenanceView = new MaintenanceView();
                    maintenanceView.setId(r.getId());
                    maintenanceView.setDescription(r.getDescription());
                    maintenanceView.setStatus(r.getStatus());
                    maintenanceView.setCreatedAt(r.getCreatedAt());

                    maintenanceView.setPropertyId(r.getPropertyId());

                    Property p = propertyById.get(r.getPropertyId());
                    if (p != null) {
                        maintenanceView.setPropertyTitle(p.getTitle());
                        maintenanceView.setPropertyCity(p.getCity());
                        if (p.getOwner() != null) {
                            maintenanceView.setOwnerName(p.getOwner().getUsername());
                        }
                    }

                    User tenant = usersById.get(r.getTenantId());
                    if (tenant != null) {
                        maintenanceView.setTenantName(tenant.getUsername());
                    }

                    return maintenanceView;
                })
                .toList();
    }

    public List<MaintenanceView> getAll() {
        List<MaintenanceResponse> responses = maintenanceClient.getAll();
        if (responses.isEmpty()) {
            return List.of();
        }

        List<UUID> propertyIds = responses.stream()
                .map(MaintenanceResponse::getPropertyId)
                .distinct()
                .toList();

        List<UUID> tenantIds = responses.stream()
                .map(MaintenanceResponse::getTenantId)
                .distinct()
                .toList();

        List<Property> properties = propertyService.getAllByIds(propertyIds);
        Map<UUID, Property> propertyById = properties.stream()
                .collect(Collectors.toMap(Property::getId, p -> p));

        Map<UUID, User> tenantsById = userService.getAllByIds(tenantIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return responses.stream()
                .map(r -> {
                    MaintenanceView maintenanceView = new MaintenanceView();
                    maintenanceView.setId(r.getId());
                    maintenanceView.setDescription(r.getDescription());
                    maintenanceView.setStatus(r.getStatus());
                    maintenanceView.setCreatedAt(r.getCreatedAt());

                    maintenanceView.setPropertyId(r.getPropertyId());

                    Property property = propertyById.get(r.getPropertyId());
                    if (property != null) {
                        maintenanceView.setPropertyTitle(property.getTitle());
                        maintenanceView.setPropertyCity(property.getCity());
                        if (property.getOwner() != null) {
                            maintenanceView.setOwnerName(property.getOwner().getUsername());
                        }
                    }

                    User tenant = tenantsById.get(r.getTenantId());
                    if (tenant != null) {
                        maintenanceView.setTenantName(tenant.getUsername());
                    }

                    return maintenanceView;
                })
                .toList();
    }

    public void updateStatus(UUID id, MaintenanceStatus newStatus) {
        MaintenanceUpdateStatusRequest request = new MaintenanceUpdateStatusRequest();
        request.setStatus(newStatus);
        maintenanceClient.updateStatus(id, request);
    }

    public void delete(UUID id) {
        maintenanceClient.delete(id);
    }

    public int getCountForOwner(UUID ownerId) {
        return getForOwner(ownerId).size();
    }

    public Map<UUID, Long> getCountsByPropertyForOwner(UUID ownerId) {
        return getForOwner(ownerId).stream()
                .filter(v -> v.getPropertyId() != null)
                .collect(Collectors.groupingBy(MaintenanceView::getPropertyId, Collectors.counting()));
    }
}
