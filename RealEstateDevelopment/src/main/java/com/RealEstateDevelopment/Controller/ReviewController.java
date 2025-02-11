package com.RealEstateDevelopment.Controller;
import com.RealEstateDevelopment.Entity.Review;
import com.RealEstateDevelopment.Service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/reviews")
public class ReviewController {
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        logger.info("Received request to fetch all reviews");
        List<Review> reviews = reviewService.getAllReviews();
        logger.info("Returning {} reviews", reviews.size());
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody Review review) {
        logger.info("Received request to submit a review: {}", review);
        try {
            Review savedReview = reviewService.saveReview(review);
            logger.info("Review saved successfully with ID: {}", savedReview.getId());
            return ResponseEntity.ok(savedReview);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred while saving review", e);
            return ResponseEntity.internalServerError().body("An error occurred while processing your request.");
        }
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        logger.info("Fetching review with ID: {}", id);
        Optional<Review> review = reviewService.getReviewById(id);

        if (review.isPresent()) {
            return ResponseEntity.ok(review.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review updatedReview) {
        logger.info("Updating review with ID: {}", id);
        Optional<Review> review = reviewService.updateReview(id, updatedReview);

        if (review.isPresent()) {
            return ResponseEntity.ok(review.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        logger.info("Deleting review with ID: {}", id);
        boolean deleted = reviewService.deleteReview(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
