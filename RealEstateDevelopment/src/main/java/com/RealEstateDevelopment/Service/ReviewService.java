package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    public List<Review> getAllReviews();
    public Review saveReview(Review review);
    public Optional<Review> getReviewById(Long id);
    public Optional<Review> updateReview(Long id, Review updatedReview);
    public boolean deleteReview(Long id);
}
