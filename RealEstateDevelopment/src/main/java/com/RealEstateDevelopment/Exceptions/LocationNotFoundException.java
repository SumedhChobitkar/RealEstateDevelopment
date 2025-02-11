package com.RealEstateDevelopment.Exceptions;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String message ) {
        super(message);
    }
}