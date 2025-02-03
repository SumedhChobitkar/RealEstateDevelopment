package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByPaymentStatus(String paymentStatus);



}

