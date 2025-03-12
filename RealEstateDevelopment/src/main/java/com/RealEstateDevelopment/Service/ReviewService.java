package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Review;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ReviewService {
    public Review addReview(Review review, MultipartFile image, MultipartFile video) throws IOException;
    public Optional<Review> getReviewById(Long reviewId);

    List<Review> getAllReviews();
}
