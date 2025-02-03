package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Booking;
import com.RealEstateDevelopment.Service.BookingService;
import com.RealEstateDevelopment.dto.BookingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/saveBooking")
    public ResponseEntity<String> saveBooking(@RequestBody BookingDto booking) {
        return bookingService.saveBooking(booking);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable Long userId) {
        return bookingService.getBookingsByUserId(userId);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/getAllPendingPayments")
    public ResponseEntity<List<Booking>> getAllPendingPayments(){
        return bookingService.getAllPendingPayments();

    }
    @PutMapping("/updateBooking")
    public ResponseEntity<String> updateBooking(Long id, BookingDto updatedBooking){
        return bookingService.updateBooking(id, updatedBooking);
    }
    @DeleteMapping("/deleteBooking")
    public ResponseEntity<String> deleteBooking(Long id){
        return bookingService.deleteBooking(id);
    }
    @GetMapping("/getBookingAvailable")
    public ResponseEntity<Boolean> isBookingAvailable(Long propertyId){
        return bookingService.isBookingAvailable(propertyId);

    }
}
