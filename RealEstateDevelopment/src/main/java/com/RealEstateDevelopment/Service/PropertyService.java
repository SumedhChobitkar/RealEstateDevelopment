package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Property;
import com.RealEstateDevelopment.Entity.TemporaryProperty;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PropertyService {

//    Property saveProperty(Property property, MultipartFile[] files);
//
//    Property updateProperty(Long id, Property property, List<MultipartFile> files);
//
//    void deleteProperty(Long id);
//
//    Property getPropertyById(Long id);
//
//    List<Property> getAllProperties();
//
//    List<Property> searchProperties(String propertyType, Double minPrice, Double maxPrice,
//                                    Integer bedrooms, Integer bathrooms, String location);
public TemporaryProperty saveProperty(Property property, MultipartFile[] files, Long agentId);
    //Property saveProperty(Property property, MultipartFile[] files);

    Property updateProperty(Long id, Property property, List<MultipartFile> files);

    void deleteProperty(Long id);

    void deleteProperty(Long id, String loggedInUserRole);

    Property getPropertyById(Long id);

    List<Property> getAllProperties();

//    List<Property> searchProperties(String propertyType, Double minPrice, Double maxPrice,
//                                    Integer bedrooms, Integer bathrooms, String location);

    String approveProperty(Long tempPropertyId, Long adminId);

    public String rejectProperty(Long tempPropertyId, Long adminId);
    public List<TemporaryProperty> getAllPendingProperties();

    List<Property> searchProperties(String propertyType, Double minPrice, Double maxPrice,
                                    Integer bedrooms, Integer bathrooms, String location, Double price);
}
