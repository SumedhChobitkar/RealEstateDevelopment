package com.RealEstateDevelopment.Service;
import com.RealEstateDevelopment.Entity.Booking;
import com.RealEstateDevelopment.dto.BookingDto;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    ResponseEntity<String> saveBooking(BookingDto booking);
    ResponseEntity<Booking> getBookingById(Long id);
    ResponseEntity<List<Booking>> getBookingsByUserId(Long userId);
    ResponseEntity<List<Booking>> getAllBookings();
    ResponseEntity<List<Booking>> getAllPendingPayments();
    ResponseEntity<String> updateBooking(Long id, BookingDto updatedBooking);
    ResponseEntity<String> deleteBooking(Long id);
    ResponseEntity<Boolean> isBookingAvailable(Long propertyId);
}
