package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.Review;
import com.RealEstateDevelopment.Repository.ReviewRepository;
import com.RealEstateDevelopment.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    // Method to add a new review, accepting image and video as MultipartFile
    public Review addReview(Review review, MultipartFile image, MultipartFile video) throws IOException {
        byte[] imageData = image != null ? image.getBytes() : null;
        byte[] videoData = video != null ? video.getBytes() : null;

        review.setImageData(imageData);
        review.setVideoData(videoData);

        return reviewRepository.save(review);
    }

    // Method to get a review by its ID
    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    @Override
    public List<Review> getAllReviews() {
        // write method to get all reviews
        return reviewRepository.findAll();
    }
}

