package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("SELECT l FROM Location l WHERE l.property.title = :title")
    List<Location> findByPropertyTitle(@Param("title") String title);// Fetch locations by property title
    List<Location> findByName(String name);

}
