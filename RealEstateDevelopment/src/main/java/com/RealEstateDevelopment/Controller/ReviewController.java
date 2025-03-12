package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Review;
import com.RealEstateDevelopment.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    // Endpoint to add a new review with image and video
    @PostMapping("/saveReview")
    public ResponseEntity<?> addReview(@RequestParam("userId") Long userId,
                                       @RequestParam("propertyId") Long propertyId,
                                       @RequestParam("comment") String comment,
                                       @RequestParam("rating") Integer rating,
                                       @RequestParam(value = "image", required = false) MultipartFile image,
                                       @RequestParam(value = "video", required = false) MultipartFile video) {

        try {
            Review review = new Review();
            review.setUserId(userId);
            review.setPropertyId(propertyId);
            review.setComment(comment);
            review.setRating(rating);

            // Call the service to add the review
            Review savedReview = reviewService.addReview(review, image, video);

            logger.info("Review added successfully: " + savedReview);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
        } catch (IOException e) {
            logger.error("Error uploading files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading files");
        }
    }

    // Endpoint to get a review by its ID
    @GetMapping("/reviewId/{id}")
    public ResponseEntity<?> getReview(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        if (review.isPresent()) {
            return ResponseEntity.ok(review.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found");
        }
    }

    // Endpoint to get a all review
    @GetMapping("/allReviews")
    public ResponseEntity<?> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }
}
