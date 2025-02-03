package com.RealEstateDevelopment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingDto {

    private Long userId;
    private Long propertyId;
    private String bookingStatus;
    private String paymentStatus;
}
