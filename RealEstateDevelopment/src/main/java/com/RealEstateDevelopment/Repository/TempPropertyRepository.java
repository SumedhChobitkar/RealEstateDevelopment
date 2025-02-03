package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.TemporaryProperty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempPropertyRepository extends JpaRepository<TemporaryProperty,Long> {
}
