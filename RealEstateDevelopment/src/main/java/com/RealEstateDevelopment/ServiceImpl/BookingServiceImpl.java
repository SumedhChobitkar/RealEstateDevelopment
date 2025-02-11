package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.*;
import com.RealEstateDevelopment.Repository.BookingRepository;
import com.RealEstateDevelopment.Repository.PropertyRepository;
import com.RealEstateDevelopment.Repository.UserRepository;
import com.RealEstateDevelopment.Service.BookingService;
import com.RealEstateDevelopment.dto.BookingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PropertyRepository propertyRepository;

    public ResponseEntity<String> saveBooking(BookingDto bookingDto) {
        try {
            // convert booking dto to booking entity
            User user = userRepository.findById(bookingDto.getUserId()).get();
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setDate(LocalDate.now());
            booking.setBookingStatus(BookingStatus.valueOf(bookingDto.getBookingStatus()));
            Property property = propertyRepository.findById(bookingDto.getPropertyId()).get();
            booking.setProperty(property);
            booking.setPaymentStatus(PaymentStatus.valueOf(bookingDto.getPaymentStatus()));
            bookingRepository.save(booking);
            return ResponseEntity.ok("Booking saved successfully");
        } catch (Exception e) {
            logger.error("Error saving booking", e);
            return ResponseEntity.status(500).body("Failed to save booking");
        }
    }

    public ResponseEntity<Booking> getBookingById(Long id) {
        try {
            Optional<Booking> booking = bookingRepository.findById(id);
            if (booking.isPresent()) {
                return ResponseEntity.ok(booking.get());
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } catch (Exception e) {
            logger.error("Error fetching booking", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    public ResponseEntity<List<Booking>> getBookingsByUserId( Long  userId) {
        try {
            return ResponseEntity.ok(bookingRepository.findByUserId(userId));
        } catch (Exception e) {
            logger.error("Error fetching bookings", e);
            return ResponseEntity.status(500).body(null);
        }

    }

    public ResponseEntity<List<Booking>> getAllBookings() {
        try {
            return ResponseEntity.ok(bookingRepository.findAll());
        } catch (Exception e) {
            logger.error("Error fetching all bookings", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    public ResponseEntity<List<Booking>> getAllPendingPayments() {
        try {
            return ResponseEntity.ok(bookingRepository.findByPaymentStatus("INPROCESS"));
        } catch (Exception e) {
            logger.error("Error fetching pending payments", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    public ResponseEntity<String> updateBooking(Long id, BookingDto updatedBooking) {
        try {
            Booking existingBooking = bookingRepository.findById(id).get();

            existingBooking.setUser(userRepository.findById(updatedBooking.getUserId()).get());
            existingBooking.setProperty(propertyRepository.findById(updatedBooking.getPropertyId()).get());
            existingBooking.setDate(LocalDate.now());
            existingBooking.setBookingStatus(BookingStatus.valueOf(updatedBooking.getBookingStatus()));
            existingBooking.setPaymentStatus(PaymentStatus.valueOf(updatedBooking.getPaymentStatus()));
            bookingRepository.save(existingBooking);
            return ResponseEntity.ok("Booking updated successfully");
        } catch (Exception e) {
            logger.error("Error updating booking", e);
            return ResponseEntity.status(500).body("Failed to update booking");
        }
    }

    public ResponseEntity<String> deleteBooking(Long id) {
        try {
            if (bookingRepository.existsById(id)) {
                bookingRepository.deleteById(id);
                return ResponseEntity.ok("Booking deleted successfully");
            } else {
                return ResponseEntity.status(404).body("Booking not found");
            }
        } catch (Exception e) {
            logger.error("Error deleting booking", e);
            return ResponseEntity.status(500).body("Failed to delete booking");
        }
    }

    public ResponseEntity<Boolean> isBookingAvailable(Long propertyId) {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            for (Booking booking : bookings) {
                if (booking.getProperty().getPropertyId().equals(propertyId) && booking.getBookingStatus()== BookingStatus.CONFIRMED) {
                    return ResponseEntity.ok(false);
                }
            }
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            logger.error("Error checking booking availability", e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
