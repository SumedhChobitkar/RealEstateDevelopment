package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface propertyRepository extends JpaRepository<Property,Long> {
}
