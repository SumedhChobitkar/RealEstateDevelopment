package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Property;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PropertyService {

    Property saveProperty(Property property, MultipartFile[] files);

    Property updateProperty(Long id, Property property, List<MultipartFile> files);

    void deleteProperty(Long id);

    Property getPropertyById(Long id);

    List<Property> getAllProperties();

    List<Property> searchProperties(String propertyType, Double minPrice, Double maxPrice,
                                    Integer bedrooms, Integer bathrooms, String location);
}
