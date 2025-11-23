package main.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import main.model.Property;
import main.model.User;
import main.model.enums.Status;
import main.web.dto.PropertyRequest;
import main.repository.PropertyRepository;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service

public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Cacheable("properties_search")
    public List<Property> search(String city, String keyword, Integer minBedrooms, Integer maxBedrooms) {

        log.info("Searching properties: city={}, keyword={}", city, keyword);

        List<Property> all = propertyRepository.findAll();

        return all.stream()
                .filter(p -> city == null || city.isBlank() ||
                        p.getCity().equalsIgnoreCase(city))
                .filter(p -> keyword == null || keyword.isBlank() ||
                        p.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .filter(p -> minBedrooms == null || p.getBedrooms() >= minBedrooms)
                .filter(p -> maxBedrooms == null || p.getBedrooms() <= maxBedrooms)
                .toList();
    }
    private List<String> saveImages(UUID propertyId, MultipartFile[] files) {
        List<String> urls = new ArrayList<>();

        String root = new File("src/main/resources/static/uploads/").getAbsolutePath();
        String folder = root + "/property-images/" + propertyId + "/";

        File dir = new File(folder);
        if (!dir.exists()) dir.mkdirs();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File newFile = new File(dir, filename);

            try {
                file.transferTo(newFile);
                urls.add("/uploads/property-images/" + propertyId + "/" + filename);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save image", e);
            }
        }

        return urls;
    }

    public List<Property> getByOwner(UUID ownerId) {
        return propertyRepository.findByOwner_Id(ownerId);
    }

    @Transactional
    public Property create(PropertyRequest request, UUID ownerId) {

        User owner = userRepository.findById(ownerId).orElseThrow();

        Property p = new Property();
        p.setTitle(request.getTitle());
        p.setDescription(request.getDescription());
        p.setCity(request.getCity());
        p.setAddress(request.getAddress());
        p.setBedrooms(request.getBedrooms());
        p.setBathrooms(request.getBathrooms());
        p.setAreaSqm(request.getAreaSqm());
        p.setMonthlyRent(request.getMonthlyRent());
        p.setStatus(Status.AVAILABLE);
        p.setOwner(owner);
        p.setCreatedOn(LocalDateTime.now());
        p.setUpdatedOn(LocalDateTime.now());

        propertyRepository.save(p);

        if (request.getImages() != null && request.getImages().length > 0) {
            List<String> urls = saveImages(p.getId(), request.getImages());
            p.setImageUrls(urls);
        }

        return propertyRepository.save(p);
    }


    public Property getById(UUID id) {
        return propertyRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void update(UUID id, PropertyRequest request, UUID ownerId) {
        Property p = propertyRepository.findById(id).orElseThrow();

        if (!p.getOwner().getId().equals(ownerId))
            throw new SecurityException("Cannot edit another owner's property");

        p.setTitle(request.getTitle());
        p.setDescription(request.getDescription());
        p.setCity(request.getCity());
        p.setAddress(request.getAddress());
        p.setBedrooms(request.getBedrooms());
        p.setBathrooms(request.getBathrooms());
        p.setAreaSqm(request.getAreaSqm());
        p.setMonthlyRent(request.getMonthlyRent());
        p.setUpdatedOn(LocalDateTime.now());

        propertyRepository.save(p);
        log.info("Property updated: {}", id);
    }

    @Transactional
    public void delete(UUID id, UUID ownerId) {
        Property p = propertyRepository.findById(id).orElseThrow();

        if (!p.getOwner().getId().equals(ownerId))
            throw new SecurityException("Cannot delete another owner's property");

        propertyRepository.delete(p);
        log.info("Property deleted: {}", id);
    }

    public Property getByIdForOwner(UUID propertyId, UUID ownerId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("You do not have access to this property");
        }

        return property;
    }

    public List<Property> searchAvailable(String city, String keyword, Integer minBedrooms, Integer maxBedrooms) {
        return search(city, keyword, minBedrooms, maxBedrooms)
                .stream()
                .filter(p -> p.getStatus().name().equals("AVAILABLE"))
                .toList();
    }


}

