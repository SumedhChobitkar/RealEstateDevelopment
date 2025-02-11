package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {

}
