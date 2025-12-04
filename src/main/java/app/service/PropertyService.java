    package app.service;

    import app.exception.ResourceNotFoundException;
    import jakarta.transaction.Transactional;
    import lombok.extern.slf4j.Slf4j;
    import app.model.Property;
    import app.model.User;
    import app.model.enums.Status;
    import app.web.dto.PropertyRequest;
    import app.repository.PropertyRepository;
    import app.repository.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.cache.annotation.CacheEvict;
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

        @Cacheable("property")
        public List<Property> getByOwner(UUID ownerId) {
            return propertyRepository.findByOwner_Id(ownerId);
        }

        @Transactional
        @CacheEvict(value = "property", allEntries = true)
        public Property create(PropertyRequest request, UUID ownerId) {

            User owner = userRepository.findById(ownerId).orElseThrow();

            Property property = new Property();
            property.setTitle(request.getTitle());
            property.setDescription(request.getDescription());
            property.setCity(request.getCity());
            property.setAddress(request.getAddress());
            property.setBedrooms(request.getBedrooms());
            property.setBathrooms(request.getBathrooms());
            property.setAreaSqm(request.getAreaSqm());
            property.setMonthlyRent(request.getMonthlyRent());
            property.setStatus(Status.AVAILABLE);
            property.setOwner(owner);
            property.setCreatedOn(LocalDateTime.now());
            property.setUpdatedOn(LocalDateTime.now());

            propertyRepository.save(property);

            if (request.getImages() != null && request.getImages().length > 0) {
                List<String> urls = saveImages(property.getId(), request.getImages());
                property.setImageUrls(urls);
            }

            return propertyRepository.save(property);
        }

        @Cacheable("property")
        public Property getById(UUID id) {
            return propertyRepository.findById(id).orElseThrow();
        }

        @Transactional
        @CacheEvict(value = "property", allEntries = true)
        public void update(UUID id, PropertyRequest request, UUID ownerId) {
            Property property = propertyRepository.findById(id).orElseThrow();

            if (!property.getOwner().getId().equals(ownerId))
                throw new SecurityException("Cannot edit another owner's property");

            property.setTitle(request.getTitle());
            property.setDescription(request.getDescription());
            property.setCity(request.getCity());
            property.setAddress(request.getAddress());
            property.setBedrooms(request.getBedrooms());
            property.setBathrooms(request.getBathrooms());
            property.setAreaSqm(request.getAreaSqm());
            property.setMonthlyRent(request.getMonthlyRent());
            property.setUpdatedOn(LocalDateTime.now());

            propertyRepository.save(property);
            log.info("Property updated: {}", id);
        }

        @Transactional
        @CacheEvict(value = "property", allEntries = true)
        public void delete(UUID id, UUID ownerId) {
            Property property = propertyRepository.findById(id).orElseThrow();

            if (!property.getOwner().getId().equals(ownerId))
                throw new SecurityException("Cannot delete another owner's property");

            propertyRepository.delete(property);
            log.info("Property deleted: {}", id);
        }

        @Cacheable("property")
        public Property getByIdForOwner(UUID propertyId, UUID ownerId) {

            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

            if (!property.getOwner().getId().equals(ownerId)) {
                throw new SecurityException("You do not have access to this property");
            }

            return property;
        }

        @Cacheable("property")
        public List<Property> searchAvailable(String city, String keyword, Integer minBedrooms, Integer maxBedrooms) {
            return search(city, keyword, minBedrooms, maxBedrooms)
                    .stream()
                    .filter(p -> p.getStatus().name().equals("AVAILABLE"))
                    .toList();
        }

        @Cacheable("property")
        public List<Property> getAllByOwner(UUID ownerId) {
            return propertyRepository.findAllByOwnerId(ownerId);
        }

        @Cacheable("property")
        public List<Property> getAllByIds(List<UUID> ids) {
            if (ids == null || ids.isEmpty()) {
                return List.of();
            }

            List<Property> result = new ArrayList<>();
            propertyRepository.findAllById(ids).forEach(result::add);
            return result;
        }


    }

